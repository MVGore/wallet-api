package com.wallet.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends WalletException {

    private final BigDecimal availableAmount;
    private final BigDecimal requestedAmount;

    public InsufficientFundsException(BigDecimal availableAmount, BigDecimal requestedAmount) {
        super(String.format("Insufficient funds. Available: %s, Requested: %s", availableAmount, requestedAmount),
                "INSUFFICIENT_FUNDS");
        this.availableAmount = availableAmount;
        this.requestedAmount = requestedAmount;
    }

    public BigDecimal getAvailableAmount() {
        return availableAmount;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
}
