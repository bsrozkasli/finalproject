package com.airline.scheduler.repository;

import com.airline.scheduler.entity.MilesAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for miles account operations in scheduler.
 */
@Repository
public interface MilesAccountRepository extends JpaRepository<MilesAccount, Long> {

    /**
     * Find miles account by user ID.
     */
    Optional<MilesAccount> findByUserId(String userId);

    /**
     * Check if account exists for user.
     */
    boolean existsByUserId(String userId);

    /**
     * Check if member number exists.
     */
    boolean existsByMemberNumber(String memberNumber);
}
