package com.mvgore.walletapi.service;

import com.mvgore.walletapi.dto.transaction.TransactionMapper;
import com.mvgore.walletapi.dto.transaction.TransactionResponseDTO;
import com.mvgore.walletapi.dto.transaction.ValidateTransactionResponseDTO;
import com.mvgore.walletapi.dto.user.UserMapper;
import com.mvgore.walletapi.dto.user.UserResponseDTO;
import com.mvgore.walletapi.dto.wallet.TransferResponseDTO;
import com.mvgore.walletapi.dto.wallet.WalletMapper;
import com.mvgore.walletapi.dto.wallet.WalletRequestDTO;
import com.mvgore.walletapi.dto.wallet.WalletResponseDTO;
import com.mvgore.walletapi.entity.Transaction;
import com.mvgore.walletapi.entity.TransactionType;
import com.mvgore.walletapi.entity.User;
import com.mvgore.walletapi.entity.Wallet;
import com.mvgore.walletapi.exception.ResourceNotFoundException;
import com.mvgore.walletapi.repository.TransactionRepository;
import com.mvgore.walletapi.repository.WalletRepository;
import com.mvgore.walletapi.repository.UserRepository;
import com.mvgore.walletapi.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Wallet getCurrentUserWallet() {
        CustomUserDetails userDetails = (CustomUserDetails)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Wallet wallet = walletRepository.findByUserId(userDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        if (wallet.isFrozen()) {
            throw new RuntimeException("Wallet is frozen. Contact admin.");
        }
        return wallet;
    }

    private Wallet getCurrentUserWalletForUpdate() {
        CustomUserDetails userDetails = (CustomUserDetails)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Wallet wallet = walletRepository.findByUserIdForUpdate(userDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        if (wallet.isFrozen()) {
            throw new RuntimeException("Wallet is frozen. Contact admin.");
        }
        return wallet;
    }

    private void createTransaction(Wallet wallet, BigDecimal amount, TransactionType type, String referenceId, String description) {
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setReferenceId(referenceId);
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }

    public WalletResponseDTO getBalance() {
        Wallet wallet = getCurrentUserWallet();
        return WalletMapper.toDTO(wallet);
    }

    public WalletResponseDTO addMoney(WalletRequestDTO dto) {
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        Wallet wallet = getCurrentUserWalletForUpdate();
        BigDecimal maxBalance = new BigDecimal("1000000000");
        BigDecimal newBalance = wallet.getBalance().add(dto.getAmount());
        if (newBalance.compareTo(maxBalance) > 0) {
            throw new IllegalArgumentException("Balance cannot exceed ₹10,00,00,000");
        }
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        createTransaction(wallet, dto.getAmount(), TransactionType.SELF_CREDIT, UUID.randomUUID().toString(), "Self Credit");
        return WalletMapper.toDTO(wallet);
    }

    public WalletResponseDTO spendMoney(WalletRequestDTO dto) {
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        Wallet wallet = getCurrentUserWalletForUpdate();
        if (wallet.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        wallet.setBalance(wallet.getBalance().subtract(dto.getAmount()));
        walletRepository.save(wallet);
        createTransaction(wallet, dto.getAmount(), TransactionType.SELF_DEBIT, UUID.randomUUID().toString(), "Self Debit");
        return WalletMapper.toDTO(wallet);
    }

    public TransferResponseDTO transfer(String receiverEmail, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        CustomUserDetails senderDetails = (CustomUserDetails)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long senderUserId = senderDetails.getUserId();

        User receiverUser = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Long receiverUserId = receiverUser.getId();

        if (senderUserId.equals(receiverUserId)) {
            throw new IllegalArgumentException("Cannot transfer to self");
        }

        Long firstLockId = Math.min(senderUserId, receiverUserId);
        Long secondLockId = Math.max(senderUserId, receiverUserId);

        Wallet firstWallet = walletRepository.findByUserIdForUpdate(firstLockId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        Wallet secondWallet = walletRepository.findByUserIdForUpdate(secondLockId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        Wallet senderWallet;
        Wallet receiverWallet;

        if (senderUserId.equals(firstWallet.getUser().getId())) {
            senderWallet = firstWallet;
            receiverWallet = secondWallet;
        } else {
            senderWallet = secondWallet;
            receiverWallet = firstWallet;
        }
        if (senderWallet.isFrozen()) {
            throw new RuntimeException("Wallet is frozen. Contact admin.");
        }

        // (OPTIONAL BUT GOOD)
        if (receiverWallet.isFrozen()) {
            throw new RuntimeException("Receiver wallet is frozen. Transfer not allowed.");
        }

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // 💰 TRANSFER
        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        String referenceId = UUID.randomUUID().toString();

        createTransaction(
                senderWallet,
                amount,
                TransactionType.TRANSFER_DEBIT,
                referenceId,
                "Sent to " + receiverWallet.getUser().getUsername() +
                        " (" + receiverWallet.getUser().getEmail() + ")"
        );

        createTransaction(
                receiverWallet,
                amount,
                TransactionType.TRANSFER_CREDIT,
                referenceId,
                "Received from " + senderWallet.getUser().getUsername() +
                        " (" + senderWallet.getUser().getEmail() + ")"
        );

        return new TransferResponseDTO(
                senderWallet.getUser().getEmail(),
                senderWallet.getBalance(),
                receiverWallet.getUser().getEmail(),
                receiverWallet.getBalance(),
                referenceId
        );
    }

    public List<TransactionResponseDTO> getTransactions(int page, int size) {
        Wallet wallet = getCurrentUserWallet();
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Transaction> transactions = transactionRepository.findByWallet(wallet, pageable);
        List<TransactionResponseDTO> result = new ArrayList<>();
        for (Transaction t : transactions.getContent()) {
            result.add(TransactionMapper.toDTO(t));
        }
        return result;
    }

    public List<TransactionResponseDTO> searchTransactions(int page, int size, String keyword) {
        Wallet wallet = getCurrentUserWallet();
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        Page<Transaction> transactions;

        if (keyword == null || keyword.isBlank()) {
            transactions = transactionRepository.findByWallet(wallet, pageable);
        } else {
            transactions = transactionRepository.searchByWalletAndKeyword(wallet, keyword,pageable);

            if (transactions.isEmpty()) {
                try {
                    TransactionType typeEnum = TransactionType.valueOf(keyword.toUpperCase());
                    transactions = transactionRepository.findByWalletAndType(wallet, typeEnum, pageable);
                } catch (Exception ignored) {}
            }
        }

        List<TransactionResponseDTO> result = transactions.getContent()
                .stream()
                .map(TransactionMapper::toDTO)
                .toList();
        return result;
    }

    public List<TransactionResponseDTO> filterTransactions(int page, int size, String keyword) {
        Wallet wallet = getCurrentUserWallet();
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
                transactions = transactionRepository.findByWalletAndTypeIn(wallet, types, pageable);
            } else {
                transactions = Page.empty(pageable);
            }
        } else {
            transactions = transactionRepository.findByWallet(wallet, pageable);
        }

        List<TransactionResponseDTO> result = new ArrayList<>();
        for (Transaction t : transactions.getContent()) {
            result.add(TransactionMapper.toDTO(t));
        }
        return result;
    }

    public List<TransactionResponseDTO> getAllTransactionsForDashboard() {
        Wallet wallet = getCurrentUserWallet();
        List<Transaction> transactions = transactionRepository.findByWallet(wallet, Sort.by(Sort.Direction.DESC, "timestamp"));
        List<TransactionResponseDTO> result = new ArrayList<>();
        for (Transaction t : transactions) {
            result.add(TransactionMapper.toDTO(t));
        }
        return result;
    }

    public UserResponseDTO getCurrentUser() {
        CustomUserDetails userDetails = (CustomUserDetails)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserMapper.toDTO(user);
    }

    public ValidateTransactionResponseDTO validateTransaction(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        ValidateTransactionResponseDTO dto = new ValidateTransactionResponseDTO();
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        return dto;
    }
}