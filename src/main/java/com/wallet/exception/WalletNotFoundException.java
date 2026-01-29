package com.wallet.exception;

public class WalletNotFoundException extends WalletException {

    public WalletNotFoundException(String walletId) {
        super("Wallet not found: " + walletId, "WALLET_NOT_FOUND");
    }
}
