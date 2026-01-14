package com.airline.scheduler.job;

import com.airline.scheduler.entity.Flight;
import com.airline.scheduler.entity.FlightStatus;
import com.airline.scheduler.entity.Booking;
import com.airline.scheduler.entity.MilesAccount;
import com.airline.scheduler.repository.BookingRepository;
import com.airline.scheduler.repository.FlightRepository;
import com.airline.scheduler.repository.MilesAccountRepository;
import com.airline.scheduler.repository.MilesTransactionRepository;
import com.airline.scheduler.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightStatusJobTest {

        @Mock
        private FlightRepository flightRepository;

        @Mock
        private BookingRepository bookingRepository;

        @Mock
        private MilesAccountRepository milesAccountRepository;

        @Mock
        private MilesTransactionRepository milesTransactionRepository;

        @Mock
        private EmailService emailService;

        @InjectMocks
        private FlightStatusJob flightStatusJob;

        @Test
        void updateCompletedFlights_ShouldProcessFlightsAndSendEmails() {
                // Arrange
                Flight flight = new Flight();
                flight.setId(1L);
                flight.setCode("TK123");
                flight.setStatus(FlightStatus.SCHEDULED);
                flight.setFromAirport("IST");
                flight.setToAirport("JFK");

                Booking booking = new Booking();
                booking.setId(100L);
                booking.setUserId("user1");
                booking.setUserEmail("user1@test.com");
                booking.setPricePaid(new BigDecimal("1000")); // 100 miles
                booking.setPassengerCount(1);
                booking.setRef("REF1");

                MilesAccount account = new MilesAccount();
                account.setId(1L);
                account.setBalance(500);

                when(flightRepository.findCompletedFlightsToUpdate(any(LocalDateTime.class)))
                                .thenReturn(Collections.singletonList(flight));

                when(bookingRepository.findByFlightIdAndStatus(1L, "CONFIRMED"))
                                .thenReturn(Collections.singletonList(booking));

                when(milesAccountRepository.findByUserId("user1"))
                                .thenReturn(Optional.of(account));

                when(milesAccountRepository.save(any(MilesAccount.class))).thenReturn(account);

                // Act
                flightStatusJob.updateCompletedFlights();

                // Assert
                // 1. Flight status should be updated
                verify(flightRepository).save(argThat(f -> f.getStatus() == FlightStatus.COMPLETED));

                // 2. Transaction should be saved (Miles earned)
                verify(milesTransactionRepository).save(any());

                // 3. Email should be sent
                verify(emailService).sendMilesUpdateEmail(eq("user1@test.com"), anyInt(), anyInt(), eq("TK123"));
        }
}
