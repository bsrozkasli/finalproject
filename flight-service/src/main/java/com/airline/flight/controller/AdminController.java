package com.airline.flight.controller;

import com.airline.flight.dto.FlightResponse;
import com.airline.flight.entity.Flight;
import com.airline.flight.entity.FlightStatus;
import com.airline.flight.service.FlightService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for admin operations.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final FlightService flightService;

    /**
     * Get all flights.
     */
    @GetMapping("/flights")
    public ResponseEntity<List<FlightResponse>> getAllFlights() {
        log.info("Admin: Getting all flights");
        List<FlightResponse> flights = flightService.getAllFlights();
        return ResponseEntity.ok(flights);
    }

    /**
     * Add a new flight.
     */
    @PostMapping("/flights")
    public ResponseEntity<FlightResponse> addFlight(@Valid @RequestBody AddFlightRequest request) {
        log.info("Admin: Adding new flight: {}", request.getCode());

        Flight flight = Flight.builder()
                .code(request.getCode())
                .fromAirport(request.getFromAirport())
                .toAirport(request.getToAirport())
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .price(request.getPrice())
                .capacity(request.getCapacity())
                .status(FlightStatus.SCHEDULED)
                .build();

        FlightResponse savedFlight = flightService.addFlight(flight);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFlight);
    }

    /**
     * Update flight status.
     */
    @PutMapping("/flights/{id}/status")
    public ResponseEntity<FlightResponse> updateFlightStatus(
            @PathVariable Long id,
            @RequestParam FlightStatus status) {

        log.info("Admin: Updating flight {} status to {}", id, status);
        FlightResponse flight = flightService.updateFlightStatus(id, status);
        return ResponseEntity.ok(flight);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddFlightRequest {
        private String code;
        private String fromAirport;
        private String toAirport;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private BigDecimal price;
        private Integer capacity;
    }
}
