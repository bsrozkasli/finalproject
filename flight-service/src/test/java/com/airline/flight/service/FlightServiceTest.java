package com.airline.flight.service;

import com.airline.flight.dto.FlightResponse;
import com.airline.flight.dto.FlightSearchRequest;
import com.airline.flight.entity.Flight;
import com.airline.flight.entity.FlightStatus;
import com.airline.flight.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private PricePredictionService pricePredictionService; // Mocked dependency

    @InjectMocks
    private FlightService flightService;

    @Test
    void searchFlights_ShouldUseExactDate_WhenNotFlexible() {
        // Arrange
        LocalDate date = LocalDate.of(2023, 10, 10);
        FlightSearchRequest request = FlightSearchRequest.builder()
                .fromAirport("IST")
                .toAirport("LHR")
                .departureDate(date)
                .flexible(false)
                .passengers(1)
                .page(0)
                .size(10)
                .build();

        Page<Flight> emptyPage = new PageImpl<>(Collections.emptyList());
        when(flightRepository.searchAvailableFlights(
                any(), any(), eq(date.atStartOfDay()), eq(date.atTime(LocalTime.MAX)), any(), anyInt(),
                any(Pageable.class))).thenReturn(emptyPage);

        // Act
        flightService.searchFlights(request);

        // Assert
        verify(flightRepository).searchAvailableFlights(
                eq("IST"), eq("LHR"), eq(date.atStartOfDay()), eq(date.atTime(LocalTime.MAX)),
                eq(FlightStatus.SCHEDULED), eq(1), any(Pageable.class));
    }

    @Test
    void searchFlights_ShouldExpandDateRange_WhenFlexible() {
        // Arrange
        LocalDate date = LocalDate.of(2023, 10, 10);
        FlightSearchRequest request = FlightSearchRequest.builder()
                .fromAirport("IST")
                .toAirport("LHR")
                .departureDate(date)
                .flexible(true)
                .passengers(1)
                .page(0)
                .size(10)
                .build();

        Page<Flight> emptyPage = new PageImpl<>(Collections.emptyList());
        when(flightRepository.searchAvailableFlights(
                any(), any(), any(), any(), any(), anyInt(), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        flightService.searchFlights(request);

        // Assert
        // Expect range to be +/- 3 days
        LocalDateTime expectedStart = date.minusDays(3).atStartOfDay();
        LocalDateTime expectedEnd = date.plusDays(3).atTime(LocalTime.MAX);

        verify(flightRepository).searchAvailableFlights(
                eq("IST"), eq("LHR"), eq(expectedStart), eq(expectedEnd), eq(FlightStatus.SCHEDULED), eq(1),
                any(Pageable.class));
    }
}
