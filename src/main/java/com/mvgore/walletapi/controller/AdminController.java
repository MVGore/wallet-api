package com.mvgore.walletapi.controller;

import com.mvgore.walletapi.dto.transaction.TransactionResponseDTO;
import com.mvgore.walletapi.dto.wallet.WalletRequestDTO;
import com.mvgore.walletapi.dto.wallet.WalletResponseDTO;
import com.mvgore.walletapi.dto.user.UserResponseDTO;
import com.mvgore.walletapi.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        return ResponseEntity.ok(service.getCurrentUser());
    }


    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(@RequestParam int page) {
        int size = 10; // hardcoded as you want
        List<UserResponseDTO> users = service.getAllUsers(page, size);
        return ResponseEntity.status(200).body(users);
    }

    @GetMapping("/users/count")
    public ResponseEntity<Long> getUsersCount() {
        return ResponseEntity.ok(service.getUsersCount());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.status(204).build();
    }

    @GetMapping("/wallets")
    public ResponseEntity<List<WalletResponseDTO>> getAllWallets(@RequestParam int page) {
        int size = 10;
        List<WalletResponseDTO> wallets = service.getAllWallets(page, size);
        return ResponseEntity.status(200).body(wallets);
    }

    @PostMapping("/wallet/{userId}/credit")
    public ResponseEntity<WalletResponseDTO> creditUserWallet(
            @PathVariable Long userId,
            @Valid @RequestBody WalletRequestDTO dto) {

        BigDecimal Amount=dto.getAmount();
        String description=dto.getDescription();
        WalletResponseDTO wallet = service.creditUserWallet(userId,Amount, description);
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/wallet/{userId}/debit")
    public ResponseEntity<WalletResponseDTO> debitUserWallet(
            @PathVariable Long userId,
            @Valid @RequestBody WalletRequestDTO dto) {

        BigDecimal Amount=dto.getAmount();
        String description=dto.getDescription();
        WalletResponseDTO wallet = service.debitUserWallet(userId,Amount, description);
        return ResponseEntity.ok(wallet);
    }

    @PutMapping("/wallet/freeze/{userId}")
    public ResponseEntity<String> freezeWallet(@PathVariable Long userId) {
        service.freezeWallet(userId);
        return ResponseEntity.status(200).body("Wallet frozen successfully");
    }

    @PutMapping("/wallet/unfreeze/{userId}")
    public ResponseEntity<String> unfreezeWallet(@PathVariable Long userId) {
        service.unfreezeWallet(userId);
        return ResponseEntity.status(200).body("Wallet unfrozen successfully");
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactions(@RequestParam int page) {
        int size = 9;
        List<TransactionResponseDTO> transactions = service.getAllTransactions(page, size);
        return ResponseEntity.status(200).body(transactions);
    }

    @GetMapping("/transactions/search")
    public ResponseEntity<List<TransactionResponseDTO>> searchTransactions(
            @RequestParam int page,
            @RequestParam String keyword) {

        int size = 9;
        List<TransactionResponseDTO> transactions =
                service.searchTransactions(page, size, keyword);

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/filter")
    public ResponseEntity<List<TransactionResponseDTO>> filterTransactions(
            @RequestParam int page,
            @RequestParam(required = false) String type) {

        int size = 9;
        List<TransactionResponseDTO> transactions =
                service.filterTransactions(page, size, type);

        return ResponseEntity.ok(transactions);
    }
}