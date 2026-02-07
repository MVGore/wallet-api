package com.mvgore.walletapi.exception;

public class InsufficientFundsException extends RuntimeException {

    // Constructor that accepts a message
    public InsufficientFundsException(String message) {
        super(message);
    }

    // Optional: no-args constructor
    public InsufficientFundsException() {
        super("Insufficient funds");
    }
}
