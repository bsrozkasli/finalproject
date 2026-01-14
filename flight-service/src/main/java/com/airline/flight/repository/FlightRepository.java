package com.airline.flight.repository;

import com.airline.flight.entity.Flight;
import com.airline.flight.entity.FlightStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Flight entity operations.
 */
@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

        /**
         * Find a flight by its code.
         */
        Optional<Flight> findByCode(String code);

        /**
         * Find a flight by ID with pessimistic lock for booking operations.
         */
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT f FROM Flight f WHERE f.id = :id")
        Optional<Flight> findByIdWithLock(@Param("id") Long id);

        /**
         * Search flights by origin and destination.
         */
        List<Flight> findByFromAirportAndToAirportAndStatus(
                        String fromAirport,
                        String toAirport,
                        FlightStatus status);

        /**
         * Search flights by origin, destination, and departure date range.
         */
        @Query("SELECT f FROM Flight f WHERE f.fromAirport = :from AND f.toAirport = :to " +
                        "AND f.departureTime >= :startDate AND f.departureTime < :endDate " +
                        "AND f.status = :status ORDER BY f.departureTime")
        List<Flight> searchFlights(
                        @Param("from") String fromAirport,
                        @Param("to") String toAirport,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("status") FlightStatus status);

        /**
         * Find flights with available seats.
         */
        @Query("SELECT f FROM Flight f WHERE f.fromAirport = :from AND f.toAirport = :to " +
                        "AND f.departureTime >= :startDate AND f.departureTime < :endDate " +
                        "AND f.status = :status AND (f.capacity - f.bookedSeats) >= :requiredSeats " +
                        "ORDER BY f.price ASC")
        List<Flight> searchAvailableFlights(
                        @Param("from") String fromAirport,
                        @Param("to") String toAirport,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("status") FlightStatus status,
                        @Param("requiredSeats") int requiredSeats);

        @Query("SELECT f FROM Flight f WHERE f.fromAirport = :from AND f.toAirport = :to " +
                        "AND f.departureTime >= :startDate AND f.departureTime < :endDate " +
                        "AND f.status = :status AND (f.capacity - f.bookedSeats) >= :requiredSeats " +
                        "ORDER BY f.price ASC")
        org.springframework.data.domain.Page<Flight> searchAvailableFlights(
                        @Param("from") String fromAirport,
                        @Param("to") String toAirport,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("status") FlightStatus status,
                        @Param("requiredSeats") int requiredSeats,
                        org.springframework.data.domain.Pageable pageable);

        /**
         * Find all flights that have arrived but are still marked as SCHEDULED.
         * Used by the scheduler to update completed flights.
         */
        @Query("SELECT f FROM Flight f WHERE f.status = 'SCHEDULED' AND f.arrivalTime < :now")
        List<Flight> findCompletedFlightsToUpdate(@Param("now") LocalDateTime now);

        /**
         * Find flights departing within a time range.
         */
        List<Flight> findByDepartureTimeBetweenAndStatus(
                        LocalDateTime start,
                        LocalDateTime end,
                        FlightStatus status);

        /**
         * Check if a flight code exists.
         */
        @Query(value = "SELECT DISTINCT to_airport FROM flights WHERE UPPER(from_airport) = UPPER(:fromAirport) AND status = 'SCHEDULED'", nativeQuery = true)
        List<String> findDistinctToAirportByFromAirport(@Param("fromAirport") String fromAirport);

        /**
         * Check if a flight code exists.
         */
        boolean existsByCode(String code);

        /**
         * Find distinct departure dates for a given route.
         */
        @Query(value = "SELECT DISTINCT CAST(departure_time AS DATE) FROM flights " +
                        "WHERE from_airport = :from AND to_airport = :to AND status = 'SCHEDULED' " +
                        "AND departure_time >= :startDate", nativeQuery = true)
        List<java.time.LocalDate> findDistinctDepartureDates(
                        @Param("from") String from,
                        @Param("to") String to,
                        @Param("startDate") LocalDateTime startDate);
}
