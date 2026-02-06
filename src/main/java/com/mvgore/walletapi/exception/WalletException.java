package com.mvgore.walletapi.exception;

public class WalletException extends RuntimeException {

    private final String code;

    public WalletException(String message, String code) {
        super(message);
        this.code = code;
    }

    public WalletException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
