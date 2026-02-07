package com.wallet.service;

import com.mvgore.walletapi.entity.Wallet;
import com.mvgore.walletapi.exception.InsufficientFundsException;
import com.mvgore.walletapi.exception.WalletNotFoundException;
import com.mvgore.walletapi.repository.TransactionRepository;
import com.mvgore.walletapi.repository.WalletRepository;
import com.mvgore.walletapi.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class WalletServiceTest {

    private WalletRepository walletRepository;
    private TransactionRepository transactionRepository;
    private WalletService walletService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        walletService = new WalletService(walletRepository, transactionRepository);
        userId = UUID.randomUUID();
    }

    @Test
    void createWalletForUser_success() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        Wallet walletToSave = new Wallet(BigDecimal.ZERO, userId);
        when(walletRepository.save(Mockito.any(Wallet.class))).thenReturn(walletToSave);

        Wallet created = walletService.createWalletForUser(userId);
        assertThat(created.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(created.getUserId()).isEqualTo(userId);
    }

    @Test
    void createWalletForUser_alreadyExists() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(new Wallet(BigDecimal.ZERO, userId)));

        assertThrows(IllegalStateException.class,
                () -> walletService.createWalletForUser(userId));
    }

    @Test
    void creditWallet_success() {
        Wallet wallet = new Wallet(BigDecimal.valueOf(100), userId);
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        Wallet updated = walletService.credit(userId, BigDecimal.valueOf(50));
        assertThat(updated.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(150));
        verify(transactionRepository, times(1)).save(Mockito.any());
    }

    @Test
    void debitWallet_success() {
        Wallet wallet = new Wallet(BigDecimal.valueOf(200), userId);
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        Wallet updated = walletService.debit(userId, BigDecimal.valueOf(150));
        assertThat(updated.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(50));
        verify(transactionRepository, times(1)).save(Mockito.any());
    }

    @Test
    void debitWallet_insufficientFunds() {
        Wallet wallet = new Wallet(BigDecimal.valueOf(100), userId);
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientFundsException.class,
                () -> walletService.debit(userId, BigDecimal.valueOf(150)));
    }

    @Test
    void getWalletByUser_notFound() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class,
                () -> walletService.getWalletByUser(userId));
    }
}
