package com.mvgore.walletapi.controller;

import com.mvgore.walletapi.entity.Wallet;
import com.mvgore.walletapi.service.WalletService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/{id}/credit")
    public Wallet credit(@PathVariable UUID id,
                         @RequestParam BigDecimal amount) {
        return walletService.credit(id, amount);
    }

    @PostMapping("/{id}/debit")
    public Wallet debit(@PathVariable UUID id,
                        @RequestParam BigDecimal amount) {
        return walletService.debit(id, amount);
    }

    @GetMapping("/{id}")
    public Wallet getWallet(@PathVariable UUID id) {
        return walletService.getWallet(id);
    }
}
