package com.airline.scheduler.repository;

import com.airline.scheduler.entity.Flight;
import com.airline.scheduler.entity.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for flight operations in scheduler.
 */
@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    /**
     * Find all flights that have arrived but are still marked as SCHEDULED.
     */
    @Query("SELECT f FROM Flight f WHERE f.status = 'SCHEDULED' AND f.arrivalTime < :now")
    List<Flight> findCompletedFlightsToUpdate(@Param("now") LocalDateTime now);

    /**
     * Find flights by status.
     */
    List<Flight> findByStatus(FlightStatus status);
}
