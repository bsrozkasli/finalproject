package com.airline.flight.repository;

import com.airline.flight.entity.MilesAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for MilesAccount entity operations.
 */
@Repository
public interface MilesAccountRepository extends JpaRepository<MilesAccount, Long> {

    /**
     * Find a miles account by user ID.
     */
    Optional<MilesAccount> findByUserId(String userId);

    /**
     * Find a miles account by member number.
     */
    Optional<MilesAccount> findByMemberNumber(String memberNumber);

    /**
     * Check if an account exists for a user.
     */
    boolean existsByUserId(String userId);

    /**
     * Check if a member number is already taken.
     */
    boolean existsByMemberNumber(String memberNumber);
}
