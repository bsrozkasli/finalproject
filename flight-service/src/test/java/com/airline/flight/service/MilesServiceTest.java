package com.airline.flight.service;

import com.airline.flight.dto.MilesAccountResponse;
import com.airline.flight.entity.MilesAccount;
import com.airline.flight.exception.InsufficientMilesException;
import com.airline.flight.repository.MilesAccountRepository;
import com.airline.flight.repository.MilesTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MilesServiceTest {

    @Mock
    private MilesAccountRepository milesAccountRepository;

    @Mock
    private MilesTransactionRepository milesTransactionRepository;

    @InjectMocks
    private MilesService milesService;

    private MilesAccount milesAccount;

    @BeforeEach
    void setUp() {
        milesAccount = MilesAccount.builder()
                .id(1L)
                .userId("test-user")
                .balance(1000)
                .memberNumber("M12345")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void earnMiles_ShouldIncreaseBalance_WhenDataIsValid() {
        // Arrange
        when(milesAccountRepository.findByUserId("test-user")).thenReturn(Optional.of(milesAccount));
        when(milesAccountRepository.save(any(MilesAccount.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        MilesAccountResponse response = milesService.earnMiles("test-user", 500, "Test Earn", "REF1");

        // Assert
        assertEquals(1500, response.getBalance());
        verify(milesTransactionRepository, times(1)).save(any());
        verify(milesAccountRepository, times(1)).save(any());
    }

    @Test
    void burnMiles_ShouldDecreaseBalance_WhenEnoughMiles() {
        // Arrange
        when(milesAccountRepository.findByUserId("test-user")).thenReturn(Optional.of(milesAccount));
        when(milesAccountRepository.save(any(MilesAccount.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        MilesAccountResponse response = milesService.burnMiles("test-user", 500, "Test Burn", "REF2");

        // Assert
        assertEquals(500, response.getBalance());
        verify(milesTransactionRepository, times(1)).save(any());
    }

    @Test
    void burnMiles_ShouldThrowException_WhenInsufficientMiles() {
        // Arrange
        when(milesAccountRepository.findByUserId("test-user")).thenReturn(Optional.of(milesAccount));

        // Act & Assert
        assertThrows(InsufficientMilesException.class,
                () -> milesService.burnMiles("test-user", 2000, "Test Burn", "REF3"));

        verify(milesTransactionRepository, never()).save(any());
    }
}
