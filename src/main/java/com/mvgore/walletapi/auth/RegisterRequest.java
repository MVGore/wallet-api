package com.mvgore.walletapi.auth;

public class RegisterRequest {
    private String username;
    private String password;

    public RegisterRequest() {}

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
