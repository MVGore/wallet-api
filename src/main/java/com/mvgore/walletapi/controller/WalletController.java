package com.mvgore.walletapi.controller;

import com.mvgore.walletapi.dto.WalletOperationRequest;
import com.mvgore.walletapi.entity.Wallet;
import com.mvgore.walletapi.service.WalletService;
import com.mvgore.walletapi.auth.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/create")
    public Wallet createWallet(@AuthenticationPrincipal User user) {
        return walletService.createWalletForUser(user.getId());
    }

    @PostMapping("/credit")
    public Wallet credit(@AuthenticationPrincipal User user,
                         @RequestBody WalletOperationRequest request) {
        return walletService.credit(user.getId(), request.getAmount());
    }

    @PostMapping("/debit")
    public Wallet debit(@AuthenticationPrincipal User user,
                        @RequestBody WalletOperationRequest request) {
        return walletService.debit(user.getId(), request.getAmount());
    }

    @GetMapping("/balance")
    public Wallet getBalance(@AuthenticationPrincipal User user) {
        return walletService.getWalletByUser(user.getId());
    }
}
