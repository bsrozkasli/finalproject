package com.airline.scheduler.repository;

import com.airline.scheduler.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for booking operations in scheduler.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find confirmed bookings for a flight.
     */
    List<Booking> findByFlightIdAndStatus(Long flightId, String status);
}
