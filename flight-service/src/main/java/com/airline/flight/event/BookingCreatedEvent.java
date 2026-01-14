package com.airline.flight.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Event published when a booking is created successfully.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreatedEvent implements Serializable {

    private String bookingRef;
    private String userId;
    private String userEmail;
    private String flightCode;
    private String fromAirport;
    private String toAirport;
    private LocalDateTime departureTime;
    private BigDecimal pricePaid;
    private Integer passengerCount;
    private List<String> passengerNames;
    private LocalDateTime createdAt;
}
