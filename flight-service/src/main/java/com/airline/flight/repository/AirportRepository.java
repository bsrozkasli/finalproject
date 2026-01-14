package com.airline.flight.repository;

import com.airline.flight.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirportRepository extends JpaRepository<Airport, String> {

    List<Airport> findByCityContainingIgnoreCaseOrNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String city, String name, String code);

    // Find airports where city, name, or code starts with or contains the query
    // (case-insensitive) - Prioritizes "starts with" matches
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Airport a WHERE " +
            "LOWER(a.city) LIKE LOWER(CONCAT(:query, '%')) OR " +
            "LOWER(a.code) LIKE LOWER(CONCAT(:query, '%')) OR " +
            "LOWER(a.name) LIKE LOWER(CONCAT(:query, '%')) OR " +
            "LOWER(a.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY " +
            "CASE WHEN LOWER(a.code) LIKE LOWER(CONCAT(:query, '%')) THEN 1 " +
            "     WHEN LOWER(a.city) LIKE LOWER(CONCAT(:query, '%')) THEN 2 " +
            "     WHEN LOWER(a.name) LIKE LOWER(CONCAT(:query, '%')) THEN 3 " +
            "     WHEN LOWER(a.code) LIKE LOWER(CONCAT('%', :query, '%')) THEN 4 " +
            "     WHEN LOWER(a.city) LIKE LOWER(CONCAT('%', :query, '%')) THEN 5 " +
            "     ELSE 6 END, " +
            "a.city ASC, a.code ASC")
    List<Airport> searchAirports(@org.springframework.data.repository.query.Param("query") String query);

    // Find all unique cities (optional, useful for dropdowns)
    // @Query("SELECT DISTINCT a.city FROM Airport a")
    // List<String> findDistinctCities();
}
