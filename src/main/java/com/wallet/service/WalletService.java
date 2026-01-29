package com.wallet.service;

import com.wallet.dto.WalletOperationRequest;
import com.wallet.dto.WalletOperationResponse;
import com.wallet.dto.WalletBalanceResponse;
import com.wallet.entity.Wallet;
import com.wallet.entity.Transaction;
import com.wallet.exception.WalletNotFoundException;
import com.wallet.exception.InsufficientFundsException;
import com.wallet.repository.WalletRepository;
import com.wallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public WalletOperationResponse processOperation(WalletOperationRequest request) {
        log.debug("Processing operation for wallet: {}, type: {}, amount: {}",
                request.getWalletId(), request.getOperationType(), request.getAmount());

        // Use pessimistic lock to handle concurrent requests
        Wallet wallet = walletRepository.findByWalletIdWithLock(request.getWalletId())
                .orElseThrow(() -> {
                    log.warn("Wallet not found: {}", request.getWalletId());
                    return new WalletNotFoundException(request.getWalletId().toString());
                });

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal newBalance;

        if (WalletOperationRequest.OperationType.DEPOSIT == request.getOperationType()) {
            newBalance = balanceBefore.add(request.getAmount());
            log.info("Deposit operation: wallet={}, amount={}, balanceBefore={}, balanceAfter={}",
                    request.getWalletId(), request.getAmount(), balanceBefore, newBalance);
        } else {
            // WITHDRAW
            if (balanceBefore.compareTo(request.getAmount()) < 0) {
                log.warn("Insufficient funds: wallet={}, available={}, requested={}",
                        request.getWalletId(), balanceBefore, request.getAmount());
                throw new InsufficientFundsException(balanceBefore, request.getAmount());
            }
            newBalance = balanceBefore.subtract(request.getAmount());
            log.info("Withdrawal operation: wallet={}, amount={}, balanceBefore={}, balanceAfter={}",
                    request.getWalletId(), request.getAmount(), balanceBefore, newBalance);
        }

        wallet.setBalance(newBalance);
        wallet = walletRepository.save(wallet);

        // Record transaction
        Transaction transaction = Transaction.builder()
                .walletId(request.getWalletId())
                .operationType(mapOperationType(request.getOperationType()))
                .amount(request.getAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(newBalance)
                .build();
        transactionRepository.save(transaction);

        return WalletOperationResponse.builder()
                .walletId(wallet.getWalletId())
                .balance(wallet.getBalance())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Transactional(readOnly = true)
    public WalletBalanceResponse getBalance(UUID walletId) {
        log.debug("Retrieving balance for wallet: {}", walletId);

        Wallet wallet = walletRepository.findByWalletId(walletId)
                .orElseThrow(() -> {
                    log.warn("Wallet not found: {}", walletId);
                    return new WalletNotFoundException(walletId.toString());
                });

        return WalletBalanceResponse.builder()
                .walletId(wallet.getWalletId())
                .balance(wallet.getBalance())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Transactional
    public Wallet createWallet(UUID walletId, BigDecimal initialBalance) {
        log.info("Creating new wallet: {}, initialBalance: {}", walletId, initialBalance);

        Wallet wallet = Wallet.builder()
                .walletId(walletId)
                .balance(initialBalance)
                .build();

        return walletRepository.save(wallet);
    }

    private Transaction.OperationType mapOperationType(WalletOperationRequest.OperationType operationType) {
        return operationType == WalletOperationRequest.OperationType.DEPOSIT ?
                Transaction.OperationType.DEPOSIT :
                Transaction.OperationType.WITHDRAW;
    }
}
