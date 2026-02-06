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
     * Main operation: DEPOSIT / WITHDRAW
     * - Concurrency safe (PESSIMISTIC_WRITE)
     * - Auto-creates wallet on first DEPOSIT
     * - Rejects invalid operations
     */
    public Wallet process(UUID walletId, String operation, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Wallet wallet = walletRepository.findByIdForUpdate(walletId)
                .orElseGet(() -> {
                    if (!"DEPOSIT".equalsIgnoreCase(operation)) {
                        throw new WalletNotFoundException(walletId);
                    }
                    Wallet w = new Wallet();
                    w.setId(walletId);
                    w.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(w);
                });

        if ("WITHDRAW".equalsIgnoreCase(operation)) {
            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException();
            }
            wallet.setBalance(wallet.getBalance().subtract(amount));
        }
        else if ("DEPOSIT".equalsIgnoreCase(operation)) {
            wallet.setBalance(wallet.getBalance().add(amount));
        }
        else {
            throw new IllegalArgumentException("Invalid operation type");
        }

        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID());
        tx.setWalletId(walletId);
        tx.setAmount(amount);
        tx.setOperation(operation);
        tx.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(tx);

        return wallet;
    }

    /**
     * Get wallet balance
     */
    public Wallet getWallet(UUID id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException(id));
    }

    /**
     * Optional helpers
     */
    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    public Wallet saveWallet(Wallet wallet) {
        return walletRepository.save(wallet);
    }
}
