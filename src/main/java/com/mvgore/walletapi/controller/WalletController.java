package com.mvgore.walletapi.controller;

import com.mvgore.walletapi.dto.WalletOperationRequest;
import com.mvgore.walletapi.dto.WalletBalanceResponse;
import com.mvgore.walletapi.entity.Wallet;
import com.mvgore.walletapi.service.WalletService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }
    public enum OperationType {
    DEPOSIT, WITHDRAW
}


    @PostMapping("/wallet")
        public WalletBalanceResponse operate(@RequestBody WalletOperationRequest request) {

            // 1 Validate that walletId is not null and is a proper UUID
            if (request.getWalletId() == null) {
                throw new IllegalArgumentException("Wallet ID must not be null");
            }

            UUID walletId;
            try {
                walletId = UUID.fromString(request.getWalletId().toString());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid Wallet ID format");
            }

            // 2 Validate operation type
            String operation = request.getOperationType();
            if (operation == null || (!operation.equalsIgnoreCase("DEPOSIT") && !operation.equalsIgnoreCase("WITHDRAW"))) {
                throw new IllegalArgumentException("Operation type must be DEPOSIT or WITHDRAW");
            }

            // 3 Validate amount
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be greater than 0");
            }

            // 4 Call service
            Wallet wallet = walletService.process(walletId, operation, request.getAmount());

            // 5 Return response
            return new WalletBalanceResponse(wallet.getId(), wallet.getBalance());
        }


    @GetMapping("/wallets/{id}")
    public WalletBalanceResponse balance(@PathVariable UUID id) {
        Wallet wallet = walletService.getWallet(id);
        return new WalletBalanceResponse(wallet.getId(), wallet.getBalance());
    }

    // ✅ Add this to fetch all wallets
    @GetMapping("/wallets")
        public List<WalletBalanceResponse> allWallets() {
            return walletService.getAllWallets()
                    .stream()
                    .map(w -> new WalletBalanceResponse(w.getId(), w.getBalance()))
                    .toList();
        }

        @PostMapping("/wallet/create")
            public WalletBalanceResponse createWallet() {
            Wallet wallet = new Wallet();
            wallet.setId(UUID.randomUUID());
            wallet.setBalance(BigDecimal.ZERO); // start with zero balance
            walletService.saveWallet(wallet);
            return new WalletBalanceResponse(wallet.getId(), wallet.getBalance());
}


}
