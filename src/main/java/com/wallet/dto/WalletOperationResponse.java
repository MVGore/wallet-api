package com.wallet.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class WalletOperationResponse {
    private UUID walletId;
    private BigDecimal oldBalance;
    private BigDecimal newBalance;

    public UUID getWalletId() { return walletId; }
    public void setWalletId(UUID walletId) { this.walletId = walletId; }

    public BigDecimal getOldBalance() { return oldBalance; }
    public void setOldBalance(BigDecimal oldBalance) { this.oldBalance = oldBalance; }

    public BigDecimal getNewBalance() { return newBalance; }
    public void setNewBalance(BigDecimal newBalance) { this.newBalance = newBalance; }

    public enum OperationType {
        DEPOSIT, WITHDRAW
    }
}
