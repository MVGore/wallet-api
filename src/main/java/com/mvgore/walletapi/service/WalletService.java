package com.mvgore.walletapi.service;

import com.mvgore.walletapi.entity.Wallet;
import com.mvgore.walletapi.exception.InsufficientFundsException;
import com.mvgore.walletapi.repository.TransactionRepository;
import com.mvgore.walletapi.repository.WalletRepository;
import com.mvgore.walletapi.service.WalletService;

import java.math.BigDecimal;
import java.util.UUID;

public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository,
                         TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    // Controller expects this
    public Wallet credit(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow();

        wallet.deposit(amount);
        return walletRepository.save(wallet);
    }

    // Controller expects this
    public Wallet debit(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow();

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(wallet.getBalance(), amount);
        }

        wallet.withdraw(amount);
        return walletRepository.save(wallet);
    }

    // Controller expects this
    public Wallet getWallet(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow();
    }
}
