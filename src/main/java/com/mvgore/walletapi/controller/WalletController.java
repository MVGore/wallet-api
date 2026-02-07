package com.mvgore.walletapi.controller;

import com.mvgore.walletapi.dto.WalletOperationRequest;
import com.mvgore.walletapi.dto.WalletBalanceResponse;
import com.mvgore.walletapi.entity.Wallet;
import com.mvgore.walletapi.service.WalletService;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    /**
     * PROCESS DEPOSIT / WITHDRAW
     * POST /api/v1/wallet
     */
    @PostMapping("/wallet")
    public WalletBalanceResponse operate(@RequestBody WalletOperationRequest request) {

        if (request.getWalletId() == null) {
            throw new IllegalArgumentException("Wallet ID must not be null");
        }

        UUID walletId;
        try {
            walletId = UUID.fromString(request.getWalletId().toString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Wallet ID format");
        }

        String operation = request.getOperationType();
        if (operation == null ||
                (!operation.equalsIgnoreCase("DEPOSIT")
                        && !operation.equalsIgnoreCase("WITHDRAW"))) {
            throw new IllegalArgumentException("Operation type must be DEPOSIT or WITHDRAW");
        }

        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        Wallet wallet = walletService.process(walletId, operation, request.getAmount());
        return new WalletBalanceResponse(wallet.getId(), wallet.getBalance());
    }

    /**
     * GET WALLET BALANCE (NEW PATH)
     * GET /api/v1/wallet/{id}
     */
    @GetMapping("/wallet/{id}")
    public WalletBalanceResponse getWallet(@PathVariable UUID id) {
        Wallet wallet = walletService.getWallet(id);
        return new WalletBalanceResponse(wallet.getId(), wallet.getBalance());
    }

    /**
     * GET WALLET BALANCE (LEGACY / TEST PATH)
     * GET /api/v1/wallets/{id}
     */
    @GetMapping("/wallets/{id}")
    public WalletBalanceResponse getWalletLegacy(@PathVariable UUID id) {
        Wallet wallet = walletService.getWallet(id);
        return new WalletBalanceResponse(wallet.getId(), wallet.getBalance());
    }

    /**
     * GET ALL WALLETS
     * GET /api/v1/wallets
     */
    @GetMapping("/wallets")
    public List<WalletBalanceResponse> allWallets() {
        return walletService.getAllWallets()
                .stream()
                .map(w -> new WalletBalanceResponse(w.getId(), w.getBalance()))
                .toList();
    }

    /**
     * CREATE EMPTY WALLET (OPTIONAL)
     * POST /api/v1/wallet/create
     */
    @PostMapping("/wallet/create")
    public WalletBalanceResponse createWallet() {
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setBalance(BigDecimal.ZERO);

        walletService.saveWallet(wallet);
        return new WalletBalanceResponse(wallet.getId(), wallet.getBalance());
    }
}
