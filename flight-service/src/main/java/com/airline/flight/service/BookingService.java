package com.airline.flight.service;

import com.airline.flight.dto.*;
import com.airline.flight.entity.*;
import com.airline.flight.event.BookingCreatedEvent;
import com.airline.flight.exception.BookingNotFoundException;
import com.airline.flight.exception.FlightFullException;
import com.airline.flight.repository.BookingRepository;
import com.airline.flight.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing bookings with concurrency control.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final FlightService flightService;
    private final PricePredictionService pricePredictionService;
    private final MilesService milesService;
    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE_NAME = "airline.topic";
    private static final String BOOKING_ROUTING_KEY = "booking.created";

    /**
     * Create a new booking with pessimistic locking to prevent overbooking.
     */
    @Transactional
    public BookingResponse createBooking(BookingRequest request, String userId, String userEmail) {
        log.info("Creating booking for flight {} by user {} with email {}", request.getFlightId(), userId, userEmail);

        int passengerCount = request.getPassengers().size();

        // Get flight with pessimistic lock to prevent race conditions
        Flight flight = flightService.getFlightWithLock(request.getFlightId());

        // Check availability
        if (!flight.hasAvailableSeats(passengerCount)) {
            throw new FlightFullException("Not enough seats available on flight " + flight.getCode() +
                    ". Requested: " + passengerCount + ", Available: " + flight.getAvailableSeats());
        }

        // Calculate total price using predicted pricing
        BigDecimal predictedPrice = pricePredictionService.predictPrice(flight);
        BigDecimal totalPrice = predictedPrice.multiply(BigDecimal.valueOf(passengerCount));

        // Initialize payment method
        com.airline.flight.model.PaymentMethod paymentMethod = com.airline.flight.model.PaymentMethod.valueOf(
                request.getPaymentMethod() != null ? request.getPaymentMethod() : "CREDIT_CARD");

        // Handle Payment
        if (paymentMethod == com.airline.flight.model.PaymentMethod.MILES) {
            // Rate: 1 Mile = 1 Unit of Currency
            int milesRequired = totalPrice.intValue();

            log.info("Processing MILES payment for user {}. Amount: {} miles", userId, milesRequired);

            // Burn miles - this will throw exception if insufficient
            milesService.burnMiles(userId, milesRequired, "Flight Booking " + flight.getCode(), "TEMP_REF");
        }

        // Create booking
        Booking booking = Booking.builder()
                .ref(generateBookingReference())
                .flight(flight)
                .userId(userId)
                .userEmail(userEmail)
                .status(BookingStatus.CONFIRMED)
                .pricePaid(totalPrice)
                .paymentMethod(paymentMethod)
                .passengerCount(passengerCount)
                .build();

        // Add passengers
        for (PassengerRequest passengerRequest : request.getPassengers()) {
            Passenger passenger = Passenger.builder()
                    .firstName(passengerRequest.getFirstName())
                    .lastName(passengerRequest.getLastName())
                    .passportNo(passengerRequest.getPassportNo())
                    .dateOfBirth(passengerRequest.getDateOfBirth())
                    .nationality(passengerRequest.getNationality())
                    .build();
            booking.addPassenger(passenger);
        }

        // Book the seats on the flight
        flight.bookSeats(passengerCount);
        flightRepository.save(flight);

        // Save booking
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully: {}", savedBooking.getRef());

        // Publish event to RabbitMQ
        publishBookingCreatedEvent(savedBooking);

        return mapToResponse(savedBooking);
    }

    /**
     * Get booking by reference.
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingByRef(String ref) {
        Booking booking = bookingRepository.findByRef(ref)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with reference: " + ref));
        return mapToResponse(booking);
    }

    /**
     * Get all bookings for a user.
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUserId(String userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancel a booking.
     */
    @Transactional
    public BookingResponse cancelBooking(String ref, String userId) {
        log.info("Cancelling booking: {} by user: {}", ref, userId);

        Booking booking = bookingRepository.findByRef(ref)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with reference: " + ref));

        // Verify ownership
        if (!booking.getUserId().equals(userId)) {
            throw new IllegalStateException("User is not authorized to cancel this booking");
        }

        // Check if cancellable
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed booking");
        }

        // Release seats
        Flight flight = booking.getFlight();
        flight.setBookedSeats(flight.getBookedSeats() - booking.getPassengerCount());
        flightRepository.save(flight);

        // Cancel booking
        booking.cancel();
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Booking cancelled: {}", ref);
        return mapToResponse(savedBooking);
    }

    /**
     * Get bookings for a flight (for scheduler).
     */
    @Transactional(readOnly = true)
    public List<Booking> getConfirmedBookingsForFlight(Long flightId) {
        return bookingRepository.findByFlightIdWithPassengers(flightId, BookingStatus.CONFIRMED);
    }

    private String generateBookingReference() {
        String ref;
        do {
            ref = "BK" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (bookingRepository.existsByRef(ref));
        return ref;
    }

    private void publishBookingCreatedEvent(Booking booking) {
        log.info("Publishing BookingCreatedEvent for booking: {}", booking.getRef());

        try {
            Flight flight = booking.getFlight();
            if (flight == null) {
                log.error("Flight is null for booking: {}", booking.getRef());
                return;
            }

            List<String> passengerNames = booking.getPassengers().stream()
                    .map(Passenger::getFullName)
                    .collect(Collectors.toList());

            BookingCreatedEvent event = BookingCreatedEvent.builder()
                    .bookingRef(booking.getRef())
                    .userId(booking.getUserId())
                    .userEmail(booking.getUserEmail())
                    .flightCode(flight.getCode())
                    .fromAirport(flight.getFromAirport())
                    .toAirport(flight.getToAirport())
                    .departureTime(flight.getDepartureTime())
                    .pricePaid(booking.getPricePaid())
                    .passengerCount(booking.getPassengerCount())
                    .passengerNames(passengerNames)
                    .createdAt(LocalDateTime.now())
                    .build();

            log.debug("Event details: {}", event);

            if (rabbitTemplate == null) {
                log.error("RabbitTemplate is null! Cannot publish event.");
                return;
            }

            rabbitTemplate.convertAndSend(EXCHANGE_NAME, BOOKING_ROUTING_KEY, event);
        } catch (Exception e) {
            log.error("❌ Failed to publish BookingCreatedEvent for booking: {}", booking.getRef(), e);
        }
    }

    private BookingResponse mapToResponse(Booking booking) {
        Flight flight = booking.getFlight();

        FlightResponse flightResponse = FlightResponse.builder()
                .id(flight.getId())
                .code(flight.getCode())
                .fromAirport(flight.getFromAirport())
                .toAirport(flight.getToAirport())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .price(flight.getPrice())
                .status(flight.getStatus())
                .durationMinutes(flight.getDurationMinutes())
                .build();

        List<PassengerResponse> passengerResponses = booking.getPassengers().stream()
                .map(p -> PassengerResponse.builder()
                        .id(p.getId())
                        .firstName(p.getFirstName())
                        .lastName(p.getLastName())
                        .passportNo(p.getPassportNo())
                        .dateOfBirth(p.getDateOfBirth())
                        .nationality(p.getNationality())
                        .build())
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .id(booking.getId())
                .ref(booking.getRef())
                .flight(flightResponse)
                .userId(booking.getUserId())
                .userEmail(booking.getUserEmail())
                .status(booking.getStatus())
                .pricePaid(booking.getPricePaid())
                .passengerCount(booking.getPassengerCount())
                .passengers(passengerResponses)
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
