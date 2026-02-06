package com.wallet.service;

import com.mvgore.walletapi.entity.Wallet;
import com.mvgore.walletapi.exception.InsufficientFundsException;
import com.mvgore.walletapi.exception.WalletNotFoundException;
import com.mvgore.walletapi.repository.TransactionRepository;
import com.mvgore.walletapi.repository.WalletRepository;
import com.mvgore.walletapi.service.WalletService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceTest {

    private WalletRepository walletRepository;
    private TransactionRepository transactionRepository;
    private WalletService walletService;

    private UUID walletId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        walletService = new WalletService(walletRepository, transactionRepository);

        walletId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("1000.00"));
    }

    @Test
    void depositIncreasesBalance() {
        when(walletRepository.findByIdForUpdate(walletId)).thenReturn(Optional.of(wallet));

        Wallet result = walletService.process(walletId, "DEPOSIT", new BigDecimal("500"));

        assertEquals(new BigDecimal("1500.00"), result.getBalance());
        verify(walletRepository).findByIdForUpdate(walletId);
        verify(transactionRepository).save(any());
    }

    @Test
    void withdrawDecreasesBalance() {
        when(walletRepository.findByIdForUpdate(walletId)).thenReturn(Optional.of(wallet));

        Wallet result = walletService.process(walletId, "WITHDRAW", new BigDecimal("400"));

        assertEquals(new BigDecimal("600.00"), result.getBalance());
        verify(transactionRepository).save(any());
    }

    @Test
    void withdrawThrowsInsufficientFunds() {
        when(walletRepository.findByIdForUpdate(walletId)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientFundsException.class, () ->
                walletService.process(walletId, "WITHDRAW", new BigDecimal("1500")));
    }

    @Test
    void walletNotFoundThrowsException() {
        when(walletRepository.findByIdForUpdate(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () ->
                walletService.process(walletId, "DEPOSIT", new BigDecimal("100")));
    }
}
