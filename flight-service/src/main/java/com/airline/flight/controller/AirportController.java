package com.airline.flight.controller;

import com.airline.flight.entity.Airport;
import com.airline.flight.repository.AirportRepository;
import com.airline.flight.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/airports")
@RequiredArgsConstructor
@Slf4j
public class AirportController {

    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;

    /**
     * Search airports by city, name or code.
     * Prioritizes matches that start with the query.
     */
    @GetMapping
    public ResponseEntity<List<Airport>> searchAirports(@RequestParam String query) {
        log.info("Searching airports with query: {}", query);
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<Airport> airports = airportRepository.searchAirports(query.trim());
        return ResponseEntity.ok(airports);
    }

    /**
     * Get available destinations from a specific airport.
     * This helps the frontend to show only valid 'To' options.
     */
    @GetMapping("/destinations")
    public ResponseEntity<List<Airport>> getDestinations(@RequestParam String from) {
        log.info("Getting valid destinations from: {}", from);

        // 1. Get distinct destination codes from Flight table
        List<String> destinationCodes = flightRepository.findDistinctToAirportByFromAirport(from);

        // 2. Fetch Airport details for these codes
        List<Airport> airports = airportRepository.findAllById(destinationCodes);

        return ResponseEntity.ok(airports);
    }

    /**
     * Search available destinations from a specific airport with a query filter.
     * Used for autocomplete in the 'To' field.
     * If query is empty, returns all valid destinations.
     */
    @GetMapping("/destinations/search")
    public ResponseEntity<List<Airport>> searchDestinations(
            @RequestParam String from,
            @RequestParam(required = false, defaultValue = "") String query) {
        log.info("Searching destinations from: {} with query: {}", from, query);

        // 1. Get distinct destination codes from Flight table
        List<String> destinationCodes = flightRepository.findDistinctToAirportByFromAirport(from.toUpperCase());

        // 2. Fetch Airport details for these codes
        List<Airport> airports = airportRepository.findAllById(destinationCodes);

        // 3. Filter by query if provided (prioritize starts with, then contains)
        if (query != null && !query.trim().isEmpty()) {
            String queryLower = query.toLowerCase().trim();
            airports = airports.stream()
                    .filter(airport -> 
                        airport.getCode().toLowerCase().startsWith(queryLower) ||
                        airport.getCity().toLowerCase().startsWith(queryLower) ||
                        airport.getName().toLowerCase().startsWith(queryLower) ||
                        airport.getCode().toLowerCase().contains(queryLower) ||
                        airport.getCity().toLowerCase().contains(queryLower) ||
                        airport.getName().toLowerCase().contains(queryLower)
                    )
                    .sorted((a1, a2) -> {
                        // Sort: code starts > city starts > name starts > contains
                        String q = queryLower;
                        boolean a1CodeStarts = a1.getCode().toLowerCase().startsWith(q);
                        boolean a2CodeStarts = a2.getCode().toLowerCase().startsWith(q);
                        boolean a1CityStarts = a1.getCity().toLowerCase().startsWith(q);
                        boolean a2CityStarts = a2.getCity().toLowerCase().startsWith(q);
                        boolean a1NameStarts = a1.getName().toLowerCase().startsWith(q);
                        boolean a2NameStarts = a2.getName().toLowerCase().startsWith(q);
                        
                        if (a1CodeStarts != a2CodeStarts) return a1CodeStarts ? -1 : 1;
                        if (a1CityStarts != a2CityStarts) return a1CityStarts ? -1 : 1;
                        if (a1NameStarts != a2NameStarts) return a1NameStarts ? -1 : 1;
                        return a1.getCity().compareToIgnoreCase(a2.getCity());
                    })
                    .toList();
        }

        return ResponseEntity.ok(airports);
    }
}
