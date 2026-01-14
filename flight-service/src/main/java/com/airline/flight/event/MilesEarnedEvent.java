package com.airline.flight.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Event published when miles are earned by a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilesEarnedEvent implements Serializable {

    private String userId;
    private String memberNumber;
    private Integer amount;
    private String description;
    private String referenceId;
    private Integer newBalance;
}
