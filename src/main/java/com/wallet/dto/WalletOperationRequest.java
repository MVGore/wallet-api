package com.wallet.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class WalletOperationRequest {
    private UUID walletId;
    private String operationType;
    private BigDecimal amount;

    public UUID getWalletId() { return walletId; }
    public void setWalletId(UUID walletId) { this.walletId = walletId; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public enum OperationType {
        DEPOSIT, WITHDRAW
    }
}
