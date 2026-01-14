package com.airline.flight.service;

import com.airline.flight.dto.FlightResponse;
import com.airline.flight.dto.FlightSearchRequest;
import com.airline.flight.entity.Flight;
import com.airline.flight.entity.FlightStatus;
import com.airline.flight.exception.FlightNotFoundException;
import com.airline.flight.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing flights.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlightService {

    private final FlightRepository flightRepository;
    private final PricePredictionService pricePredictionService;

    /**
     * Search for available flights.
     * Results are cached for performance.
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<FlightResponse> searchFlights(FlightSearchRequest request) {
        log.info("Searching flights from {} to {} on {} (Page: {}, Size: {})",
                request.getFromAirport(),
                request.getToAirport(),
                request.getDepartureDate(),
                request.getPage(),
                request.getSize());

        LocalDate departureDate = request.getDepartureDate();
        if (departureDate == null) {
            departureDate = LocalDate.now();
        }

        LocalDateTime startOfDay;
        LocalDateTime endOfDay;

        if (Boolean.TRUE.equals(request.getFlexible())) {
            log.info("Flexible search requested. Expanding date range.");
            startOfDay = departureDate.minusDays(3).atStartOfDay();
            endOfDay = departureDate.plusDays(3).atTime(LocalTime.MAX);
        } else {
            startOfDay = departureDate.atStartOfDay();
            endOfDay = departureDate.atTime(LocalTime.MAX);
        }

        int requiredSeats = request.getPassengers() != null ? request.getPassengers() : 1;
        int page = request.getPage() < 0 ? 0 : request.getPage();
        int size = request.getSize() <= 0 ? 10 : request.getSize();

        org.springframework.data.domain.Page<Flight> flightsPage = flightRepository.searchAvailableFlights(
                request.getFromAirport().toUpperCase(),
                request.getToAirport().toUpperCase(),
                startOfDay,
                endOfDay,
                FlightStatus.SCHEDULED,
                requiredSeats,
                org.springframework.data.domain.PageRequest.of(page, size));

        log.info("Found {} flights matching search criteria", flightsPage.getTotalElements());

        return flightsPage.map(this::mapToResponse);
    }

    /**
     * Get a flight by ID.
     */
    @Transactional(readOnly = true)
    public FlightResponse getFlightById(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with id: " + id));
        return mapToResponse(flight);
    }

    /**
     * Get a flight by code.
     */
    @Transactional(readOnly = true)
    public FlightResponse getFlightByCode(String code) {
        Flight flight = flightRepository.findByCode(code)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with code: " + code));
        return mapToResponse(flight);
    }

    /**
     * Get the flight entity by ID (for internal use).
     */
    @Transactional(readOnly = true)
    public Flight getFlightEntityById(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with id: " + id));
    }

    /**
     * Get the flight entity by ID with lock (for booking).
     */
    @Transactional
    public Flight getFlightWithLock(Long id) {
        return flightRepository.findByIdWithLock(id)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with id: " + id));
    }

    /**
     * Add a new flight (admin only).
     */
    @CacheEvict(value = "flights", allEntries = true)
    @Transactional
    public FlightResponse addFlight(Flight flight) {
        log.info("Adding new flight: {}", flight.getCode());

        if (flightRepository.existsByCode(flight.getCode())) {
            throw new IllegalArgumentException("Flight with code " + flight.getCode() + " already exists");
        }

        flight.setStatus(FlightStatus.SCHEDULED);
        Flight savedFlight = flightRepository.save(flight);

        log.info("Flight added successfully: {}", savedFlight.getCode());
        return mapToResponse(savedFlight);
    }

    /**
     * Update flight status.
     */
    @CacheEvict(value = "flights", allEntries = true)
    @Transactional
    public FlightResponse updateFlightStatus(Long id, FlightStatus status) {
        Flight flight = getFlightEntityById(id);
        flight.setStatus(status);
        Flight updatedFlight = flightRepository.save(flight);

        log.info("Flight {} status updated to {}", flight.getCode(), status);
        return mapToResponse(updatedFlight);
    }

    /**
     * Get all flights (for admin).
     */
    @Transactional(readOnly = true)
    public List<FlightResponse> getAllFlights() {
        return flightRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find distinct departure dates for a given route.
     */
    @Transactional(readOnly = true)
    public List<LocalDate> getAvailableDates(String from, String to) {
        log.info("Fetching dates for route {} -> {} and filtering in Java", from, to);
        List<Flight> flights = flightRepository.findByFromAirportAndToAirportAndStatus(
                from.toUpperCase(),
                to.toUpperCase(),
                FlightStatus.SCHEDULED);

        return flights.stream()
                .filter(f -> f.getDepartureTime().isAfter(LocalDateTime.now()))
                .map(f -> f.getDepartureTime().toLocalDate())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Find completed flights that need status update (for scheduler).
     */
    @Transactional(readOnly = true)
    public List<Flight> findCompletedFlightsToUpdate() {
        return flightRepository.findCompletedFlightsToUpdate(LocalDateTime.now());
    }

    /**
     * Map flight entity to response DTO.
     */
    private FlightResponse mapToResponse(Flight flight) {
        return FlightResponse.builder()
                .id(flight.getId())
                .code(flight.getCode())
                .fromAirport(flight.getFromAirport())
                .toAirport(flight.getToAirport())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .price(flight.getPrice())
                .predictedPrice(pricePredictionService.predictPrice(flight))
                .capacity(flight.getCapacity())
                .availableSeats(flight.getAvailableSeats())
                .status(flight.getStatus())
                .durationMinutes(flight.getDurationMinutes())
                .build();
    }
}
