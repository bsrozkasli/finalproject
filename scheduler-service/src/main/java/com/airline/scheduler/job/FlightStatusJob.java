package com.airline.scheduler.job;

import com.airline.scheduler.entity.*;
import com.airline.scheduler.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job to update flight statuses and trigger miles earning.
 * Runs every night at 00:00.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FlightStatusJob {

    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;
    private final MilesAccountRepository milesAccountRepository;
    private final MilesTransactionRepository milesTransactionRepository;
    private final com.airline.scheduler.service.EmailService emailService;

    private static final int MILES_PER_PRICE_UNIT = 10; // 1 mile per $10

    /**
     * Update completed flights and trigger miles earning.
     * Runs at midnight every day.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void updateCompletedFlights() {
        log.info("========================================");
        log.info("Starting FlightStatusJob - Nightly Flight Update");
        log.info("========================================");

        LocalDateTime now = LocalDateTime.now();
        List<Flight> completedFlights = flightRepository.findCompletedFlightsToUpdate(now);

        log.info("Found {} flights to mark as COMPLETED", completedFlights.size());

        int totalMilesEarned = 0;
        int passengersProcessed = 0;

        for (Flight flight : completedFlights) {
            try {
                log.info("Processing flight: {} ({} -> {})",
                        flight.getCode(), flight.getFromAirport(), flight.getToAirport());

                // Update flight status
                flight.setStatus(FlightStatus.COMPLETED);
                flight.setUpdatedAt(LocalDateTime.now());
                flightRepository.save(flight);

                // Process miles for all confirmed bookings
                List<Booking> bookings = bookingRepository.findByFlightIdAndStatus(
                        flight.getId(), "CONFIRMED");

                log.info("Found {} confirmed bookings for flight {}", bookings.size(), flight.getCode());

                for (Booking booking : bookings) {
                    int milesEarned = processMilesForBooking(booking, flight);
                    totalMilesEarned += milesEarned;
                    passengersProcessed += booking.getPassengerCount();
                }

                log.info("Flight {} marked as COMPLETED", flight.getCode());

            } catch (Exception e) {
                log.error("Failed to process flight: {}", flight.getCode(), e);
                // Continue with other flights
            }
        }

        log.info("========================================");
        log.info("FlightStatusJob completed");
        log.info("Flights processed: {}", completedFlights.size());
        log.info("Passengers processed: {}", passengersProcessed);
        log.info("Total miles awarded: {}", totalMilesEarned);
        log.info("========================================");
    }

    /**
     * Process miles for a booking after flight completion.
     */
    private int processMilesForBooking(Booking booking, Flight flight) {
        try {
            // Calculate miles based on price paid
            int milesPerPassenger = booking.getPricePaid().intValue() / MILES_PER_PRICE_UNIT;
            int totalMiles = milesPerPassenger * booking.getPassengerCount();

            if (totalMiles <= 0) {
                return 0;
            }

            // Get or create miles account
            MilesAccount account = milesAccountRepository.findByUserId(booking.getUserId())
                    .orElseGet(() -> createMilesAccount(booking.getUserId(), booking.getUserEmail()));

            // Update balance
            account.setBalance(account.getBalance() + totalMiles);
            account.setUpdatedAt(LocalDateTime.now());

            // Update tier based on new balance
            updateTier(account);

            milesAccountRepository.save(account);

            // Create transaction record
            MilesTransaction transaction = MilesTransaction.builder()
                    .accountId(account.getId())
                    .amount(totalMiles)
                    .type("EARN")
                    .description("Flight " + flight.getCode() + " (" + flight.getFromAirport() +
                            " -> " + flight.getToAirport() + ")")
                    .referenceId(booking.getRef())
                    .transactionDate(LocalDateTime.now())
                    .build();
            milesTransactionRepository.save(transaction);

            log.info("Awarded {} miles to user {} for booking {}",
                    totalMiles, booking.getUserId(), booking.getRef());

            // Send notification email (async)
            // Note: In a real scenario, we would need to look up the user's email from
            // their ID or Booking
            // Here we assume booking has userEmail field (it does as per Booking entity)
            if (booking.getUserEmail() != null && !booking.getUserEmail().isEmpty()) {
                emailService.sendMilesUpdateEmailAsync(booking.getUserEmail(), totalMiles, account.getBalance(),
                        flight.getCode())
                        .exceptionally(ex -> {
                            log.error("Failed to send miles update email asynchronously", ex);
                            return null;
                        });
            }

            return totalMiles;

        } catch (Exception e) {
            log.error("Failed to process miles for booking: {}", booking.getRef(), e);
            return 0;
        }
    }

    private MilesAccount createMilesAccount(String userId, String email) {
        String memberNumber = generateMemberNumber();

        MilesAccount account = MilesAccount.builder()
                .userId(userId)
                .memberNumber(memberNumber)
                .balance(0)
                .tier("BRONZE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        MilesAccount savedAccount = milesAccountRepository.save(account);

        if (email != null && !email.isEmpty()) {
            // Send welcome email asynchronously
            emailService.sendWelcomeEmailAsync(email, memberNumber)
                    .exceptionally(ex -> {
                        log.error("Failed to send welcome email asynchronously", ex);
                        return null;
                    });
        }

        return savedAccount;
    }

    private String generateMemberNumber() {
        String memberNumber;
        do {
            memberNumber = "ML" + String.format("%06d", (int) (Math.random() * 1000000));
        } while (milesAccountRepository.existsByMemberNumber(memberNumber));
        return memberNumber;
    }

    private void updateTier(MilesAccount account) {
        int balance = account.getBalance();
        if (balance >= 100000) {
            account.setTier("PLATINUM");
        } else if (balance >= 50000) {
            account.setTier("GOLD");
        } else if (balance >= 20000) {
            account.setTier("SILVER");
        } else {
            account.setTier("BRONZE");
        }
    }

    /**
     * Manual trigger for testing purposes.
     */
    public void runManually() {
        log.info("Manually triggering FlightStatusJob");
        updateCompletedFlights();
    }
}
