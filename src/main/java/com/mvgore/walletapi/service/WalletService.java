package com.mvgore.walletapi.service;

import com.mvgore.walletapi.entity.Transaction;
import com.mvgore.walletapi.entity.Wallet;
import com.mvgore.walletapi.exception.InsufficientFundsException;
import com.mvgore.walletapi.exception.WalletNotFoundException;
import com.mvgore.walletapi.repository.TransactionRepository;
import com.mvgore.walletapi.repository.WalletRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository,
                         TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Deposit / Withdraw
     */
    public Wallet process(UUID walletId, String operation, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Wallet wallet = walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        if ("WITHDRAW".equalsIgnoreCase(operation)) {
            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Insufficient balance");
            }
            wallet.setBalance(wallet.getBalance().subtract(amount));

        } else if ("DEPOSIT".equalsIgnoreCase(operation)) {
            wallet.setBalance(wallet.getBalance().add(amount));

        } else {
            throw new IllegalArgumentException("Invalid operation type");
        }

        walletRepository.save(wallet);

        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID());
        tx.setWalletId(walletId);
        tx.setAmount(amount);
        tx.setOperation(operation);
        tx.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(tx);

        return wallet;
    }

    public Wallet getWallet(UUID id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException(id));
    }

    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    public Wallet saveWallet(Wallet wallet) {
        return walletRepository.save(wallet);
    }
}
