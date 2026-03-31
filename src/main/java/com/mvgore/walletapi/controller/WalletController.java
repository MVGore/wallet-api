package com.mvgore.walletapi.controller;

import com.mvgore.walletapi.dto.transaction.TransactionResponseDTO;
import com.mvgore.walletapi.dto.transaction.ValidateTransactionResponseDTO;
import com.mvgore.walletapi.dto.user.UserResponseDTO;
import com.mvgore.walletapi.dto.wallet.TransferResponseDTO;
import com.mvgore.walletapi.dto.wallet.WalletRequestDTO;
import com.mvgore.walletapi.dto.wallet.WalletResponseDTO;
import com.mvgore.walletapi.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService service;

    public WalletController(WalletService service) {
        this.service = service;
    }

    @GetMapping("/balance")
    public ResponseEntity<WalletResponseDTO> getBalance(){
        WalletResponseDTO wallet = service.getBalance();
        return ResponseEntity.status(200).body(wallet);
    }

    @PostMapping("/credit")
    public ResponseEntity<WalletResponseDTO> addMoney(@Valid @RequestBody WalletRequestDTO dto){
        WalletResponseDTO wallet = service.addMoney(dto);
        return ResponseEntity.status(201).body(wallet);
    }

    @PostMapping("/debit")
    public ResponseEntity<WalletResponseDTO> spendMoney(@Valid @RequestBody WalletRequestDTO dto){
        WalletResponseDTO wallet = service.spendMoney(dto);
        return ResponseEntity.status(200).body(wallet);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactions(@RequestParam int page){
        int size = 7;
        List<TransactionResponseDTO> transactions = service.getTransactions(page, size);
        return ResponseEntity.status(200).body(transactions);
    }

    @GetMapping("/transactions/search")
    public ResponseEntity<List<TransactionResponseDTO>> searchTransactions(@RequestParam int page,
                                                                           @RequestParam String keyword){
        int size = 7;
        List<TransactionResponseDTO> transactions = service.searchTransactions(page, size, keyword);
        return ResponseEntity.status(200).body(transactions);
    }

    @GetMapping("/transactions/filter")
    public ResponseEntity<List<TransactionResponseDTO>> filterTransactions(@RequestParam int page,
                                                                           @RequestParam(required = false) String type){
        int size = 7;
        List<TransactionResponseDTO> transactions = service.filterTransactions(page, size, type);
        return ResponseEntity.status(200).body(transactions);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDTO> transferMoney(@RequestParam String email,
                                                             @RequestParam BigDecimal amount) {

        TransferResponseDTO response = service.transfer(email, amount);
        return ResponseEntity.status(200).body(response);
    }

    @GetMapping("/dashboard/transactions")
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactionsForDashboard() {
        List<TransactionResponseDTO> transactions = service.getAllTransactionsForDashboard();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        return ResponseEntity.ok(service.getCurrentUser());
    }

    @GetMapping("/validate-transaction")
    public ResponseEntity<ValidateTransactionResponseDTO> validateTransaction(@RequestParam String email) {
        return ResponseEntity.ok(service.validateTransaction(email));
    }
}