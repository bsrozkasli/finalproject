package com.airline.flight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for flight search requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSearchRequest {

    @NotBlank(message = "Departure airport is required")
    @Size(min = 3, max = 3, message = "Airport code must be 3 characters")
    private String fromAirport;

    @NotBlank(message = "Arrival airport is required")
    @Size(min = 3, max = 3, message = "Airport code must be 3 characters")
    private String toAirport;

    private LocalDate departureDate;
    private int page;
    private int size;

    @Builder.Default
    private Integer passengers = 1;

    @Builder.Default
    private Boolean flexible = false;
}
