package com.airline.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Miles transaction entity for scheduler operations.
 */
@Entity
@Table(name = "miles_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilesTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(length = 255)
    private String description;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
}
