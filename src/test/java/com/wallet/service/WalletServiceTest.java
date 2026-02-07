package com.wallet.service;

import com.mvgore.walletapi.entity.Wallet;
import com.mvgore.walletapi.entity.Transaction;
import com.mvgore.walletapi.repository.TransactionRepository;
import com.mvgore.walletapi.repository.WalletRepository;
import com.mvgore.walletapi.service.WalletService;
import com.mvgore.walletapi.exception.InsufficientFundsException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;

    private UUID walletId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        walletId = UUID.randomUUID();
        // Use the constructor since setters are gone
        wallet = new Wallet(walletId, BigDecimal.valueOf(1000));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(null); // if you need a stub
    }

    @Test
    void testCredit() {
        BigDecimal amount = BigDecimal.valueOf(500);

        Wallet updatedWallet = walletService.credit(walletId, amount);

        assertEquals(BigDecimal.valueOf(1500), updatedWallet.getBalance());
        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testDebit() {
        BigDecimal amount = BigDecimal.valueOf(300);

        Wallet updatedWallet = walletService.debit(walletId, amount);

        assertEquals(BigDecimal.valueOf(700), updatedWallet.getBalance());
        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testDebitInsufficientFunds() {
        BigDecimal amount = BigDecimal.valueOf(1500);

        Exception exception = assertThrows(InsufficientFundsException.class, () -> {
            walletService.debit(walletId, amount);
        });

        assertTrue(exception.getMessage().contains("Insufficient"));
        verify(walletRepository, never()).save(wallet);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testGetWallet() {
        Wallet foundWallet = walletService.getWallet(walletId);
        assertNotNull(foundWallet);
        assertEquals(walletId, foundWallet.getId());
    }
}
