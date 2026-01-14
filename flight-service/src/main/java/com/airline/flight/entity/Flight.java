package com.airline.flight.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a flight in the airline system.
 */
@Entity
@Table(name = "flights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "from_airport", nullable = false, length = 3)
    private String fromAirport;

    @Column(name = "to_airport", nullable = false, length = 3)
    private String toAirport;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "booked_seats", nullable = false)
    @Builder.Default
    private Integer bookedSeats = 0;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FlightStatus status = FlightStatus.SCHEDULED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Booking> bookings = new ArrayList<>();

    /**
     * Get the number of available seats.
     */
    public int getAvailableSeats() {
        return capacity - bookedSeats;
    }

    /**
     * Check if the flight has available seats.
     */
    public boolean hasAvailableSeats(int requiredSeats) {
        return getAvailableSeats() >= requiredSeats;
    }

    /**
     * Book seats on this flight.
     */
    public void bookSeats(int numberOfSeats) {
        if (!hasAvailableSeats(numberOfSeats)) {
            throw new IllegalStateException("Not enough available seats");
        }
        this.bookedSeats += numberOfSeats;
    }

    /**
     * Get flight duration in minutes.
     */
    public long getDurationMinutes() {
        return java.time.Duration.between(departureTime, arrivalTime).toMinutes();
    }
}
