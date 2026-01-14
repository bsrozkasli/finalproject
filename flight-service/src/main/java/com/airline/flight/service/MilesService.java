package com.airline.flight.service;

import com.airline.flight.dto.MilesAccountResponse;
import com.airline.flight.entity.MilesAccount;
import com.airline.flight.entity.MilesTransaction;
import com.airline.flight.exception.InsufficientMilesException;
import com.airline.flight.exception.MilesAccountNotFoundException;
import com.airline.flight.repository.MilesAccountRepository;
import com.airline.flight.repository.MilesTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing miles accounts and transactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MilesService {

    private final MilesAccountRepository milesAccountRepository;
    private final MilesTransactionRepository milesTransactionRepository;

    /**
     * Get miles account for a user.
     */
    @Transactional(readOnly = true)
    public MilesAccountResponse getAccountByUserId(String userId) {
        MilesAccount account = milesAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new MilesAccountNotFoundException("Miles account not found for user: " + userId));
        return mapToResponse(account);
    }

    /**
     * Get or create miles account for a user and return as response.
     */
    @Transactional
    public MilesAccountResponse getOrCreateAccountResponse(String userId) {
        MilesAccount account = getOrCreateAccount(userId);
        return mapToResponse(account);
    }

    /**
     * Get or create miles account for a user.
     */
    @Transactional
    public MilesAccount getOrCreateAccount(String userId) {
        return milesAccountRepository.findByUserId(userId)
                .orElseGet(() -> createAccount(userId));
    }

    /**
     * Create a new miles account.
     */
    @Transactional
    public MilesAccount createAccount(String userId) {
        log.info("Creating new miles account for user: {}", userId);

        String memberNumber = generateMemberNumber();

        MilesAccount account = MilesAccount.builder()
                .userId(userId)
                .memberNumber(memberNumber)
                .balance(0)
                .build();

        return milesAccountRepository.save(account);
    }

    /**
     * Earn miles for a user.
     */
    @Transactional
    public MilesAccountResponse earnMiles(String userId, int amount, String description, String referenceId) {
        log.info("Earning {} miles for user: {}", amount, userId);

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        MilesAccount account = getOrCreateAccount(userId);
        account.earnMiles(amount);

        // Create transaction record
        MilesTransaction transaction = MilesTransaction.createEarnTransaction(
                account, amount, description, referenceId);
        milesTransactionRepository.save(transaction);

        MilesAccount savedAccount = milesAccountRepository.save(account);
        log.info("Miles earned successfully. New balance: {}", savedAccount.getBalance());

        return mapToResponse(savedAccount);
    }

    /**
     * Burn miles for a user.
     */
    @Transactional
    public MilesAccountResponse burnMiles(String userId, int amount, String description, String referenceId) {
        log.info("Burning {} miles for user: {}", amount, userId);

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        MilesAccount account = milesAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new MilesAccountNotFoundException("Miles account not found for user: " + userId));

        if (!account.hasEnoughMiles(amount)) {
            throw new InsufficientMilesException(
                    "Insufficient miles balance. Required: " + amount + ", Available: " + account.getBalance());
        }

        account.burnMiles(amount);

        // Create transaction record
        MilesTransaction transaction = MilesTransaction.createBurnTransaction(
                account, amount, description, referenceId);
        milesTransactionRepository.save(transaction);

        MilesAccount savedAccount = milesAccountRepository.save(account);
        log.info("Miles burned successfully. New balance: {}", savedAccount.getBalance());

        return mapToResponse(savedAccount);
    }

    /**
     * Add bonus miles.
     */
    @Transactional
    public MilesAccountResponse addBonusMiles(String userId, int amount, String description) {
        log.info("Adding {} bonus miles for user: {}", amount, userId);

        MilesAccount account = getOrCreateAccount(userId);
        account.earnMiles(amount);

        // Create transaction record
        MilesTransaction transaction = MilesTransaction.createBonusTransaction(
                account, amount, description);
        milesTransactionRepository.save(transaction);

        MilesAccount savedAccount = milesAccountRepository.save(account);
        return mapToResponse(savedAccount);
    }

    /**
     * Calculate miles to earn for a flight.
     * Base calculation: 1 mile per 10 price units.
     */
    public int calculateMilesForFlight(double pricePaid) {
        return (int) (pricePaid / 10);
    }

    private String generateMemberNumber() {
        String memberNumber;
        do {
            memberNumber = "ML" + String.format("%06d", (int) (Math.random() * 1000000));
        } while (milesAccountRepository.existsByMemberNumber(memberNumber));
        return memberNumber;
    }

    private MilesAccountResponse mapToResponse(MilesAccount account) {
        int totalEarned = milesTransactionRepository.sumEarnedMiles(account.getId());
        int totalBurned = milesTransactionRepository.sumBurnedMiles(account.getId());

        return MilesAccountResponse.builder()
                .id(account.getId())
                .memberNumber(account.getMemberNumber())
                .balance(account.getBalance())
                .tier(account.getTier())
                .totalEarned(totalEarned)
                .totalBurned(totalBurned)
                .build();
    }
}
