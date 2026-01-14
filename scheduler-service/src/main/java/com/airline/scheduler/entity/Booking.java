package com.airline.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Booking entity for scheduler operations.
 */
@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String ref;

    @Column(name = "flight_id", nullable = false)
    private Long flightId;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "price_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePaid;

    @Column(name = "passenger_count", nullable = false)
    private Integer passengerCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
