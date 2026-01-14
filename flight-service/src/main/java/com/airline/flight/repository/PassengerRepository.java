package com.airline.flight.repository;

import com.airline.flight.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Passenger entity operations.
 */
@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    /**
     * Find all passengers for a booking.
     */
    List<Passenger> findByBookingId(Long bookingId);

    /**
     * Find a passenger by passport number.
     */
    Optional<Passenger> findByPassportNo(String passportNo);

    /**
     * Find passengers by passport number across all bookings.
     */
    List<Passenger> findAllByPassportNo(String passportNo);
}
