package com.airline.flight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for passenger information in booking requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Passport number is required")
    @Size(max = 20, message = "Passport number must not exceed 20 characters")
    private String passportNo;

    private LocalDate dateOfBirth;

    @Size(max = 50, message = "Nationality must not exceed 50 characters")
    private String nationality;
}
