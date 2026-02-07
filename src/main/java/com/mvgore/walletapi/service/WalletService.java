package com.mvgore.walletapi.service;

import com.mvgore.walletapi.entity.Wallet;
import com.mvgore.walletapi.entity.Transaction;
import com.mvgore.walletapi.exception.InsufficientFundsException;
import com.mvgore.walletapi.exception.WalletNotFoundException;
import com.mvgore.walletapi.repository.TransactionRepository;
import com.mvgore.walletapi.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Wallet createWalletForUser(UUID userId) {
        if(walletRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("Wallet already exists for this user");
        }

        Wallet wallet = new Wallet(BigDecimal.ZERO, userId);
        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet credit(UUID userId, BigDecimal amount) {
        Wallet wallet = getWalletByUser(userId);
        wallet.deposit(amount);
        walletRepository.save(wallet);

        transactionRepository.save(new Transaction(wallet, amount, "CREDIT"));

        return wallet;
    }

    @Transactional
    public Wallet debit(UUID userId, BigDecimal amount) {
        Wallet wallet = getWalletByUser(userId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        wallet.withdraw(amount);
        walletRepository.save(wallet);

        transactionRepository.save(new Transaction(wallet, amount, "DEBIT"));

        return wallet;
    }

    public Wallet getWalletByUser(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user"));
    }
}
