package com.mvgore.walletapi.dto;

import java.math.BigDecimal;

public class WalletOperationRequest {

    private String operationType;
    private BigDecimal amount;

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public enum OperationType {
        DEPOSIT, WITHDRAW
    }
}
