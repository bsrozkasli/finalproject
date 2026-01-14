package com.airline.flight.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity representing a miles transaction.
 */
@Entity
@Table(name = "miles_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilesTransaction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MilesAccount account;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MilesTransactionType type;

    @Column(length = 255)
    private String description;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @CreationTimestamp
    @Column(name = "transaction_date", nullable = false, updatable = false)
    private LocalDateTime transactionDate;

    /**
     * Create an earn transaction.
     */
    public static MilesTransaction createEarnTransaction(MilesAccount account, int amount, String description,
            String referenceId) {
        return MilesTransaction.builder()
                .account(account)
                .amount(amount)
                .type(MilesTransactionType.EARN)
                .description(description)
                .referenceId(referenceId)
                .build();
    }

    /**
     * Create a burn transaction.
     */
    public static MilesTransaction createBurnTransaction(MilesAccount account, int amount, String description,
            String referenceId) {
        return MilesTransaction.builder()
                .account(account)
                .amount(-amount)
                .type(MilesTransactionType.BURN)
                .description(description)
                .referenceId(referenceId)
                .build();
    }

    /**
     * Create a bonus transaction.
     */
    public static MilesTransaction createBonusTransaction(MilesAccount account, int amount, String description) {
        return MilesTransaction.builder()
                .account(account)
                .amount(amount)
                .type(MilesTransactionType.BONUS)
                .description(description)
                .build();
    }
}
