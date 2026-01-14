package com.airline.flight.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for booking creation requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @NotNull(message = "Flight ID is required")
    private Long flightId;

    @Email(message = "Valid email is required")
    private String email;

    @jakarta.validation.constraints.Pattern(regexp = "CREDIT_CARD|MILES", message = "Invalid payment method. Accepted values: CREDIT_CARD, MILES")
    private String paymentMethod = "CREDIT_CARD";

    @NotEmpty(message = "At least one passenger is required")
    @Valid
    private List<PassengerRequest> passengers;
}
