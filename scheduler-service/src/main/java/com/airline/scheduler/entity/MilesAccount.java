package com.airline.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Miles account entity for scheduler operations.
 */
@Entity
@Table(name = "miles_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilesAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "azure_user_id", nullable = false, unique = true, length = 100)
    private String userId;

    @Column(name = "member_number", nullable = false, unique = true, length = 20)
    private String memberNumber;

    @Column(nullable = false)
    private Integer balance;

    @Column(nullable = false, length = 20)
    private String tier;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
