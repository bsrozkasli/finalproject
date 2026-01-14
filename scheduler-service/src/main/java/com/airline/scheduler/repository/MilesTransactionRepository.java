package com.airline.scheduler.repository;

import com.airline.scheduler.entity.MilesTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for miles transaction operations in scheduler.
 */
@Repository
public interface MilesTransactionRepository extends JpaRepository<MilesTransaction, Long> {
}
