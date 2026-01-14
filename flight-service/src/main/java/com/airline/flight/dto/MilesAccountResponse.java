package com.airline.flight.dto;

import com.airline.flight.entity.MilesTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for miles account responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilesAccountResponse {

    private Long id;
    private String memberNumber;
    private Integer balance;
    private MilesTier tier;
    private Integer totalEarned;
    private Integer totalBurned;
}
