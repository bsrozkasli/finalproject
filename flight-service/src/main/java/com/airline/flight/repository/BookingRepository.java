package com.airline.flight.repository;

import com.airline.flight.entity.Booking;
import com.airline.flight.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Booking entity operations.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find a booking by its reference.
     */
    Optional<Booking> findByRef(String ref);

    /**
     * Find all bookings for a user.
     */
    List<Booking> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Find all bookings for a flight.
     */
    List<Booking> findByFlightIdAndStatus(Long flightId, BookingStatus status);

    /**
     * Find all bookings for a flight (for miles earning).
     */
    @Query("SELECT b FROM Booking b JOIN FETCH b.passengers WHERE b.flight.id = :flightId AND b.status = :status")
    List<Booking> findByFlightIdWithPassengers(
            @Param("flightId") Long flightId,
            @Param("status") BookingStatus status);

    /**
     * Find bookings for a user within a status.
     */
    List<Booking> findByUserIdAndStatus(String userId, BookingStatus status);

    /**
     * Check if a booking reference exists.
     */
    boolean existsByRef(String ref);

    /**
     * Count confirmed bookings for a flight.
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.flight.id = :flightId AND b.status = 'CONFIRMED'")
    int countConfirmedBookingsByFlightId(@Param("flightId") Long flightId);
}
