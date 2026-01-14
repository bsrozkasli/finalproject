package com.airline.flight.dto;

import com.airline.flight.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for booking responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;
    private String ref;
    private FlightResponse flight;
    private String userId;
    private String userEmail;
    private BookingStatus status;
    private BigDecimal pricePaid;
    private Integer passengerCount;
    private List<PassengerResponse> passengers;
    private LocalDateTime createdAt;
}
