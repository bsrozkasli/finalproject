package com.airline.flight.service;

import com.airline.flight.dto.BookingRequest;
import com.airline.flight.dto.BookingResponse;
import com.airline.flight.dto.PassengerRequest;
import com.airline.flight.entity.Booking;
import com.airline.flight.entity.BookingStatus;
import com.airline.flight.entity.Flight;
import com.airline.flight.entity.FlightStatus;
import com.airline.flight.model.PaymentMethod;
import com.airline.flight.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FlightService flightService;

    @Mock
    private MilesService milesService;

    @InjectMocks
    private BookingService bookingService;

    private Flight flight;
    private BookingRequest bookingRequest;

    @BeforeEach
    void setUp() {
        flight = new Flight();
        flight.setId(1L);
        flight.setPrice(new BigDecimal("100.00"));
        flight.setCapacity(10); // Set capacity
        flight.setBookedSeats(0); // Set booked seats, available = 10 - 0 = 10
        flight.setStatus(FlightStatus.SCHEDULED);
        flight.setCode("TK101");
        flight.setFromAirport("IST");
        flight.setToAirport("LHR");

        bookingRequest = new BookingRequest();
        bookingRequest.setFlightId(1L);
        bookingRequest.setEmail("test@example.com");

        PassengerRequest passengerRequest = new PassengerRequest();
        passengerRequest.setFirstName("John");
        passengerRequest.setLastName("Doe");
        passengerRequest.setPassportNo("P123456");

        bookingRequest.setPassengers(new ArrayList<>());
        bookingRequest.getPassengers().add(passengerRequest);
    }

    @Test
    void createBooking_ShouldSucceed_WithCreditCard() {
        // Arrange
        bookingRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD.name());

        when(flightService.getFlightWithLock(1L)).thenReturn(flight);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> {
            Booking b = (Booking) i.getArguments()[0];
            b.setId(100L);
            b.setRef("REF-123");
            b.setStatus(BookingStatus.CONFIRMED);
            return b;
        });

        // Act
        BookingResponse response = bookingService.createBooking(bookingRequest, "user1", "test@example.com");

        // Assert
        assertNotNull(response);
        assertEquals("CONFIRMED", response.getStatus());
        verify(milesService, never()).burnMiles(anyString(), anyInt(), anyString(), anyString());
    }

    @Test
    void createBooking_ShouldBurnMiles_WhenPaymentIsMiles() {
        // Arrange
        bookingRequest.setPaymentMethod(PaymentMethod.MILES.name());

        when(flightService.getFlightWithLock(1L)).thenReturn(flight);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> {
            Booking b = (Booking) i.getArguments()[0];
            b.setId(100L);
            b.setRef("REF-MILES");
            b.setStatus(BookingStatus.CONFIRMED);
            return b;
        });

        // Act
        BookingResponse response = bookingService.createBooking(bookingRequest, "user1", "test@example.com");

        // Assert
        assertNotNull(response);
        verify(milesService, times(1)).burnMiles(eq("user1"), anyInt(), anyString(), anyString());
    }
}
