package com.mvgore.walletapi.service;

import com.mvgore.walletapi.dto.transaction.TransactionMapper;
import com.mvgore.walletapi.dto.transaction.TransactionResponseDTO;
import com.mvgore.walletapi.dto.user.UserMapper;
import com.mvgore.walletapi.dto.wallet.WalletMapper;
import com.mvgore.walletapi.dto.wallet.WalletResponseDTO;
import com.mvgore.walletapi.dto.user.UserResponseDTO;
import com.mvgore.walletapi.entity.Transaction;
import com.mvgore.walletapi.entity.TransactionType;
import com.mvgore.walletapi.entity.User;
import com.mvgore.walletapi.entity.Wallet;
import com.mvgore.walletapi.exception.ResourceNotFoundException;
import com.mvgore.walletapi.repository.TransactionRepository;
import com.mvgore.walletapi.repository.UserRepository;
import com.mvgore.walletapi.repository.WalletRepository;
import com.mvgore.walletapi.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public UserResponseDTO getCurrentUser() {
        CustomUserDetails userDetails = (CustomUserDetails)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        return UserMapper.toDTO(user);
    }

    public List<UserResponseDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<User> usersPage = userRepository.findByRole("USER", pageable);

        List<UserResponseDTO> dtos = new ArrayList<>();

        for (User user : usersPage.getContent()) dtos.add(UserResponseDTO.fromEntity(user));

        return dtos;
    }

    public Long getUsersCount() {
        return userRepository.countByRole("USER");
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        walletRepository.findByUserId(userId).ifPresent(walletRepository::delete);

        userRepository.delete(user);
    }

    public List<WalletResponseDTO> getAllWallets(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Wallet> wallets = walletRepository.findAll(pageable);
        List<WalletResponseDTO> dtos = new ArrayList<>();
        for (Wallet wallet : wallets) dtos.add(WalletMapper.toDTO(wallet));
        return dtos;
    }

    public List<TransactionResponseDTO> getAllTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Transaction> transactions = transactionRepository.findAll(pageable);

        List<TransactionResponseDTO> dtos = new ArrayList<>();
        for (Transaction t : transactions) dtos.add(TransactionMapper.toDTO(t));
        return dtos;
    }

    public WalletResponseDTO creditUserWallet(Long userId, BigDecimal amount, String description) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (wallet.isFrozen()) throw new RuntimeException("Wallet is frozen");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than 0");

        wallet.setBalance(wallet.getBalance().add(amount));
        Wallet savedWallet = walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setWallet(savedWallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.ADMIN_CREDIT);
        transaction.setReferenceId(UUID.randomUUID().toString());
        transaction.setDescription(description != null ? description : "Admin credit");
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        return WalletMapper.toDTO(savedWallet);
    }

    public WalletResponseDTO debitUserWallet(Long userId, BigDecimal amount, String description) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (wallet.isFrozen()) throw new RuntimeException("Wallet is frozen");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than 0");
        if (wallet.getBalance().compareTo(amount) < 0)
            throw new IllegalArgumentException("Insufficient balance");

        wallet.setBalance(wallet.getBalance().subtract(amount));
        Wallet savedWallet = walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setWallet(savedWallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.ADMIN_DEBIT);
        transaction.setReferenceId(UUID.randomUUID().toString());
        transaction.setDescription(description != null ? description : "Admin debit");
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        return WalletMapper.toDTO(savedWallet);
    }

    public void freezeWallet(Long userId) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (wallet.isFrozen()) throw new RuntimeException("Wallet already frozen");
        wallet.setFrozen(true);
        walletRepository.save(wallet);
    }

    public void unfreezeWallet(Long userId) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (!wallet.isFrozen()) throw new RuntimeException("Wallet already active");
        wallet.setFrozen(false);
        walletRepository.save(wallet);
    }

    public List<TransactionResponseDTO> searchTransactions(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Transaction> transactions;

        if (keyword == null || keyword.isBlank()) {
            transactions = transactionRepository.findAll(pageable);
        } else {
            transactions = transactionRepository.searchAllTransactions(keyword, pageable);

            if (transactions.isEmpty()) {
                try {
                    TransactionType typeEnum = TransactionType.valueOf(keyword.toUpperCase());
                    transactions = transactionRepository.findAll(
                            PageRequest.of(page, size, Sort.by("timestamp").descending())
                    ).map(t -> t); // fallback simple
                } catch (Exception ignored) {}
            }
        }

        List<TransactionResponseDTO> result = new ArrayList<>();
        for (Transaction t : transactions.getContent()) {
            result.add(TransactionMapper.toDTO(t));
        }
        return result;
    }

    public List<TransactionResponseDTO> filterTransactions(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Transaction> transactions;

        if (keyword != null && !keyword.isEmpty()) {
            List<TransactionType> types = new ArrayList<>();

            switch (keyword.toUpperCase()) {
                case "CREDIT":
                    types.add(TransactionType.ADMIN_CREDIT);
                    types.add(TransactionType.SELF_CREDIT);
                    types.add(TransactionType.TRANSFER_CREDIT);
                    break;

                case "DEBIT":
                    types.add(TransactionType.ADMIN_DEBIT);
                    types.add(TransactionType.SELF_DEBIT);
                    types.add(TransactionType.TRANSFER_DEBIT);
                    break;

                case "TRANSFER":
                    types.add(TransactionType.TRANSFER_CREDIT);
                    types.add(TransactionType.TRANSFER_DEBIT);
                    break;
            }

            if (!types.isEmpty()) {
                transactions = transactionRepository.findByTypeIn(types, pageable);
            } else {
                transactions = Page.empty(pageable);
            }
        } else {
            transactions = transactionRepository.findAll(pageable);
        }

        List<TransactionResponseDTO> result = new ArrayList<>();
        for (Transaction t : transactions.getContent()) {
            result.add(TransactionMapper.toDTO(t));
        }
        return result;
    }
}