package com.mvgore.walletapi.controller;

import com.mvgore.walletapi.auth.User;
import com.mvgore.walletapi.auth.UserRepository;
import com.mvgore.walletapi.dto.WalletOperationRequest;
import com.mvgore.walletapi.entity.Wallet;
import com.mvgore.walletapi.service.WalletService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final WalletService walletService;
    private final UserRepository userRepository;

    public WalletController(
            WalletService walletService,
            UserRepository userRepository
    ) {
        this.walletService = walletService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));
    }

    @PostMapping("/create")
    public Wallet createWallet(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = getCurrentUser(userDetails);
        return walletService.createWalletForUser(user.getId());
    }

    @PostMapping("/credit")
    public Wallet credit(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody WalletOperationRequest request
    ) {
        User user = getCurrentUser(userDetails);
        return walletService.credit(user.getId(), request.getAmount());
    }

    @PostMapping("/debit")
    public Wallet debit(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody WalletOperationRequest request
    ) {
        User user = getCurrentUser(userDetails);
        return walletService.debit(user.getId(), request.getAmount());
    }

    @GetMapping("/balance")
    public Wallet getBalance(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = getCurrentUser(userDetails);
        return walletService.getWalletByUser(user.getId());
    }
}
