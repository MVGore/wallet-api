package com.wallet.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class WalletBalanceResponse {

    private UUID walletId;
    private BigDecimal balance;

    public enum OperationType {
        DEPOSIT, WITHDRAW
    }

    public WalletBalanceResponse(UUID walletId, BigDecimal balance) {
        this.walletId = walletId;
        this.balance = balance;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}

