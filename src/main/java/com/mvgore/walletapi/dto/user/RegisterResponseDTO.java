package com.mvgore.walletapi.dto.user;

public class RegisterResponseDTO {

    private String username;
    private String email;
    private String role;
    private String token;

    public RegisterResponseDTO(String username, String email, String role, String token) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}