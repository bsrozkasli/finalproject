package com.airline.flight.controller;

import com.airline.flight.dto.FlightResponse;
import com.airline.flight.dto.FlightSearchRequest;
import com.airline.flight.service.FlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for flight operations.
 */
@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
@Slf4j
public class FlightController {

    private final FlightService flightService;

    /**
     * Search for available flights.
     */
    @GetMapping("/search")
    public ResponseEntity<org.springframework.data.domain.Page<FlightResponse>> searchFlights(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") Integer passengers,
            @RequestParam(defaultValue = "false") Boolean flexible,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Searching flights from {} to {} on {} for {} passengers. Page: {}, Size: {}", from, to, date,
                passengers, page, size);

        FlightSearchRequest request = FlightSearchRequest.builder()
                .fromAirport(from)
                .toAirport(to)
                .departureDate(date)
                .passengers(passengers)
                .flexible(flexible)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(flightService.searchFlights(request));
    }

    /**
     * Get available dates for a route.
     */
    @GetMapping("/dates")
    public ResponseEntity<List<LocalDate>> getAvailableDates(
            @RequestParam String from,
            @RequestParam String to) {
        log.info("Fetching available dates for route: {} -> {}", from, to);
        return ResponseEntity.ok(flightService.getAvailableDates(from, to));
    }

    /**
     * Get flight by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FlightResponse> getFlightById(@PathVariable Long id) {
        log.info("Getting flight by ID: {}", id);
        FlightResponse flight = flightService.getFlightById(id);
        return ResponseEntity.ok(flight);
    }

    /**
     * Get flight by code.
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<FlightResponse> getFlightByCode(@PathVariable String code) {
        log.info("Getting flight by code: {}", code);
        FlightResponse flight = flightService.getFlightByCode(code);
        return ResponseEntity.ok(flight);
    }
}
