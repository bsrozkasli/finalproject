package com.airline.flight.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for passenger responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String passportNo;
    private LocalDate dateOfBirth;
    private String nationality;
}
