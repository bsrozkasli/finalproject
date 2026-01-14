package com.airline.flight.repository;

import com.airline.flight.entity.MilesTransaction;
import com.airline.flight.entity.MilesTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for MilesTransaction entity operations.
 */
@Repository
public interface MilesTransactionRepository extends JpaRepository<MilesTransaction, Long> {

    /**
     * Find all transactions for an account.
     */
    List<MilesTransaction> findByAccountIdOrderByTransactionDateDesc(Long accountId);

    /**
     * Find transactions for an account with pagination.
     */
    Page<MilesTransaction> findByAccountId(Long accountId, Pageable pageable);

    /**
     * Find transactions by type for an account.
     */
    List<MilesTransaction> findByAccountIdAndType(Long accountId, MilesTransactionType type);

    /**
     * Find transactions within a date range.
     */
    List<MilesTransaction> findByAccountIdAndTransactionDateBetween(
            Long accountId,
            LocalDateTime start,
            LocalDateTime end);

    /**
     * Calculate total earned miles for an account.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM MilesTransaction t " +
            "WHERE t.account.id = :accountId AND t.type = 'EARN'")
    int sumEarnedMiles(@Param("accountId") Long accountId);

    /**
     * Calculate total burned miles for an account.
     */
    @Query("SELECT COALESCE(SUM(ABS(t.amount)), 0) FROM MilesTransaction t " +
            "WHERE t.account.id = :accountId AND t.type = 'BURN'")
    int sumBurnedMiles(@Param("accountId") Long accountId);
}
