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
 * Entity representing a booking in the airline system.
 */
@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String ref;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Flight flight;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "payment_method", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private com.airline.flight.model.PaymentMethod paymentMethod = com.airline.flight.model.PaymentMethod.CREDIT_CARD;

    @Column(name = "price_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePaid;

    @Column(name = "passenger_count", nullable = false)
    @Builder.Default
    private Integer passengerCount = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Passenger> passengers = new ArrayList<>();

    /**
     * Add a passenger to this booking.
     */
    public void addPassenger(Passenger passenger) {
        passengers.add(passenger);
        passenger.setBooking(this);
    }

    /**
     * Remove a passenger from this booking.
     */
    public void removePassenger(Passenger passenger) {
        passengers.remove(passenger);
        passenger.setBooking(null);
    }

    /**
     * Confirm this booking.
     */
    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }

    /**
     * Cancel this booking.
     */
    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }
}
