package com.airline.flight.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for partner miles operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerMilesRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @Min(value = 1, message = "Amount must be positive")
    private Integer amount;

    private String description;

    private String referenceId;
}
