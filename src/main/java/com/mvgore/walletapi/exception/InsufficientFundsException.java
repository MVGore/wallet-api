package com.mvgore.walletapi.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException() {
        super("Insufficient funds to perform this operation");
    }

    public InsufficientFundsException(BigDecimal currentBalance, BigDecimal attemptedAmount) {
        super("Insufficient funds: balance is " + currentBalance + ", attempted withdrawal " + attemptedAmount);
    }
}
