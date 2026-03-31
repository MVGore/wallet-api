package com.mvgore.walletapi.dto.transaction;

public class ValidateTransactionResponseDTO {

    private String email;
    private String username;

    public ValidateTransactionResponseDTO() {
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}