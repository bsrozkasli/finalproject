package com.airline.flight.repository;

import com.airline.flight.entity.PartnerAirline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for PartnerAirline entity operations.
 */
@Repository
public interface PartnerAirlineRepository extends JpaRepository<PartnerAirline, Long> {

    /**
     * Find a partner by API key.
     */
    Optional<PartnerAirline> findByApiKey(String apiKey);

    /**
     * Find an active partner by API key.
     */
    Optional<PartnerAirline> findByApiKeyAndIsActiveTrue(String apiKey);

    /**
     * Find a partner by code.
     */
    Optional<PartnerAirline> findByCode(String code);

    /**
     * Check if an API key is valid and active.
     */
    boolean existsByApiKeyAndIsActiveTrue(String apiKey);
}
