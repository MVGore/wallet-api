package com.wallet.service;

import com.wallet.dto.WalletOperationRequest;
import com.wallet.entity.Wallet;
import com.wallet.exception.InsufficientFundsException;
import com.wallet.exception.WalletNotFoundException;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;

    private UUID walletId;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        testWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .walletId(walletId)
                .balance(BigDecimal.valueOf(1000))
                .version(0L)
                .build();
    }

    @Test
    void testDepositOperation() {
        WalletOperationRequest request = WalletOperationRequest.builder()
                .walletId(walletId)
                .operationType(WalletOperationRequest.OperationType.DEPOSIT)
                .amount(BigDecimal.valueOf(500))
                .build();

        when(walletRepository.findByWalletIdWithLock(walletId))
                .thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(testWallet);

        var response = walletService.processOperation(request);

        assertEquals(walletId, response.getWalletId());
        verify(walletRepository).findByWalletIdWithLock(walletId);
        verify(walletRepository).save(any(Wallet.class));
        verify(transactionRepository).save(any());
    }

    @Test
    void testWithdrawOperation() {
        WalletOperationRequest request = WalletOperationRequest.builder()
                .walletId(walletId)
                .operationType(WalletOperationRequest.OperationType.WITHDRAW)
                .amount(BigDecimal.valueOf(300))
                .build();

        when(walletRepository.findByWalletIdWithLock(walletId))
                .thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(testWallet);

        var response = walletService.processOperation(request);

        assertEquals(walletId, response.getWalletId());
        verify(walletRepository).findByWalletIdWithLock(walletId);
        verify(walletRepository).save(any(Wallet.class));
        verify(transactionRepository).save(any());
    }

    @Test
    void testWithdrawInsufficientFunds() {
        WalletOperationRequest request = WalletOperationRequest.builder()
                .walletId(walletId)
                .operationType(WalletOperationRequest.OperationType.WITHDRAW)
                .amount(BigDecimal.valueOf(2000))
                .build();

        when(walletRepository.findByWalletIdWithLock(walletId))
                .thenReturn(Optional.of(testWallet));

        assertThrows(InsufficientFundsException.class, () ->
                walletService.processOperation(request));

        verify(walletRepository).findByWalletIdWithLock(walletId);
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testWalletNotFoundForOperation() {
        WalletOperationRequest request = WalletOperationRequest.builder()
                .walletId(walletId)
                .operationType(WalletOperationRequest.OperationType.DEPOSIT)
                .amount(BigDecimal.valueOf(100))
                .build();

        when(walletRepository.findByWalletIdWithLock(walletId))
                .thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () ->
                walletService.processOperation(request));

        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testGetBalance() {
        when(walletRepository.findByWalletId(walletId))
                .thenReturn(Optional.of(testWallet));

        var response = walletService.getBalance(walletId);

        assertEquals(walletId, response.getWalletId());
        assertEquals(BigDecimal.valueOf(1000), response.getBalance());
        verify(walletRepository).findByWalletId(walletId);
    }

    @Test
    void testGetBalanceNotFound() {
        when(walletRepository.findByWalletId(walletId))
                .thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () ->
                walletService.getBalance(walletId));

        verify(walletRepository).findByWalletId(walletId);
    }

    @Test
    void testCreateWallet() {
        UUID newWalletId = UUID.randomUUID();
        BigDecimal initialBalance = BigDecimal.valueOf(5000);

        Wallet newWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .walletId(newWalletId)
                .balance(initialBalance)
                .build();

        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(newWallet);

        var result = walletService.createWallet(newWalletId, initialBalance);

        assertEquals(newWalletId, result.getWalletId());
        assertEquals(initialBalance, result.getBalance());
        verify(walletRepository).save(any(Wallet.class));
    }
}
