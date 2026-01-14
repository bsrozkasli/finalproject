package com.airline.flight.config;

import com.airline.flight.entity.*;
import com.airline.flight.model.PaymentMethod;
import com.airline.flight.repository.AirportRepository;
import com.airline.flight.repository.BookingRepository;
import com.airline.flight.repository.FlightRepository;
import com.airline.flight.repository.MilesAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;
    private final MilesAccountRepository milesAccountRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (airportRepository.count() == 0) {
            log.info("Seeding Airports...");
            seedAirports();
            log.info("Seeding Flights...");
            seedFlights();
            log.info("Seeding Users...");
            seedUsers();
            log.info("Seeding Bookings...");
            seedBookings();
            log.info("Database Seeding Completed.");
        }
    }

    private void seedAirports() {
        List<Airport> airports = Arrays.asList(
                new Airport("IST", "Istanbul Airport", "Istanbul", "Turkey"),
                new Airport("SAW", "Sabiha Gokcen Airport", "Istanbul", "Turkey"),
                new Airport("ESB", "Esenboga Airport", "Ankara", "Turkey"),
                new Airport("ADB", "Adnan Menderes Airport", "Izmir", "Turkey"),
                new Airport("AYT", "Antalya Airport", "Antalya", "Turkey"),
                new Airport("LHR", "Heathrow Airport", "London", "UK"),
                new Airport("LGW", "Gatwick Airport", "London", "UK"),
                new Airport("JFK", "John F. Kennedy Airport", "New York", "USA"),
                new Airport("CDG", "Charles de Gaulle Airport", "Paris", "France"),
                new Airport("FRA", "Frankfurt Airport", "Frankfurt", "Germany"),
                new Airport("MUC", "Munich Airport", "Munich", "Germany"));
        airportRepository.saveAll(airports);
    }

    private void seedFlights() {
        List<String> airportCodes = Arrays.asList("IST", "SAW", "ESB", "ADB", "AYT", "LHR", "LGW", "JFK", "CDG", "FRA",
                "MUC");
        Random random = new Random();

        for (int i = 0; i < 50; i++) {
            String from = airportCodes.get(random.nextInt(airportCodes.size()));
            String to = airportCodes.get(random.nextInt(airportCodes.size()));

            while (from.equals(to)) {
                to = airportCodes.get(random.nextInt(airportCodes.size()));
            }

            // Create flights for next 7 days
            LocalDate date = LocalDate.now().plusDays(random.nextInt(7));
            LocalTime time = LocalTime.of(random.nextInt(24), 0);
            LocalDateTime departure = LocalDateTime.of(date, time);

            int duration = 60 + random.nextInt(300); // 1-6 hours (Not set directly on entity, calculated)
            LocalDateTime arrival = departure.plusMinutes(duration);

            Flight flight = new Flight();
            flight.setCode("TK" + (1000 + i));
            flight.setFromAirport(from);
            flight.setToAirport(to);
            flight.setDepartureTime(departure);
            flight.setArrivalTime(arrival);
            // Duration is calculated
            flight.setPrice(BigDecimal.valueOf(50 + random.nextInt(450))); // 50-500 USD
            flight.setCapacity(150 + random.nextInt(100)); // 150-250 seats
            // Available seats is calculated (capacity - bookedSeats)
            flight.setBookedSeats(0);
            flight.setStatus(FlightStatus.SCHEDULED);

            flightRepository.save(flight);
        }
    }

    private void seedUsers() {
        // Create 5 dummy users with miles accounts
        for (int i = 1; i <= 5; i++) {
            MilesAccount account = MilesAccount.builder()
                    .userId("auth0|" + (1000 + i)) // Dummy Auth0 ID
                    .memberNumber("ML" + (90000 + i))
                    .balance(i * 5000) // varying balances
                    .tier(i > 3 ? MilesTier.GOLD : MilesTier.BRONZE)
                    .build();
            milesAccountRepository.save(account);
        }

        // Add a "Rich" user
        MilesAccount richUser = MilesAccount.builder()
                .userId("auth0|richuser")
                .memberNumber("ML888888")
                .balance(200000)
                .tier(MilesTier.PLATINUM)
                .build();
        milesAccountRepository.save(richUser);
    }

    private void seedBookings() {
        List<Flight> flights = flightRepository.findAll();
        Random random = new Random();

        for (int i = 0; i < 20; i++) {
            Flight flight = flights.get(random.nextInt(flights.size()));
            String userId = "auth0|" + (1001 + random.nextInt(5)); // Random user from seeded users

            Booking booking = Booking.builder()
                    .ref("REF" + (5000 + i))
                    .flight(flight)
                    .userId(userId)
                    .userEmail("user" + userId.substring(6) + "@example.com")
                    .status(BookingStatus.CONFIRMED)
                    .paymentMethod(random.nextBoolean() ? PaymentMethod.CREDIT_CARD : PaymentMethod.MILES)
                    .pricePaid(flight.getPrice())
                    .passengerCount(1)
                    .build();

            // Add passenger
            Passenger passenger = Passenger.builder()
                    .firstName("Passenger")
                    .lastName("One")
                    .passportNo("P" + (10000 + i)) // Fixed field name
                    .build();
            booking.addPassenger(passenger);

            bookingRepository.save(booking);

            // Increase booked seats
            flight.setBookedSeats(flight.getBookedSeats() + 1);
            flightRepository.save(flight);
        }
    }
}
