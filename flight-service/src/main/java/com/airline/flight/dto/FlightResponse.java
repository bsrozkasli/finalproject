package com.airline.flight.dto;

import com.airline.flight.entity.FlightStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for flight responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightResponse implements Serializable {

    private Long id;
    private String code;
    private String fromAirport;
    private String toAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal price;
    private BigDecimal predictedPrice;
    private Integer capacity;
    private Integer availableSeats;
    private FlightStatus status;
    private Long durationMinutes;
}
