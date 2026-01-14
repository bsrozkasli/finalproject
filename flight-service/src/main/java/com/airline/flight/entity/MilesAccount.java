package com.airline.flight.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a miles account for a user.
 */
@Entity
@Table(name = "miles_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilesAccount implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "azure_user_id", nullable = false, unique = true, length = 100)
    private String userId;

    @Column(name = "member_number", nullable = false, unique = true, length = 20)
    private String memberNumber;

    @Column(nullable = false)
    @Builder.Default
    private Integer balance = 0;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MilesTier tier = MilesTier.BRONZE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MilesTransaction> transactions = new ArrayList<>();

    /**
     * Earn miles to this account.
     */
    public void earnMiles(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.balance += amount;
        updateTier();
    }

    /**
     * Burn miles from this account.
     */
    public void burnMiles(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount > this.balance) {
            throw new IllegalStateException("Insufficient miles balance");
        }
        this.balance -= amount;
    }

    /**
     * Check if account has enough miles.
     */
    public boolean hasEnoughMiles(int amount) {
        return this.balance >= amount;
    }

    /**
     * Update tier based on current balance.
     */
    private void updateTier() {
        if (balance >= 100000) {
            this.tier = MilesTier.PLATINUM;
        } else if (balance >= 50000) {
            this.tier = MilesTier.GOLD;
        } else if (balance >= 20000) {
            this.tier = MilesTier.SILVER;
        } else {
            this.tier = MilesTier.BRONZE;
        }
    }
}
