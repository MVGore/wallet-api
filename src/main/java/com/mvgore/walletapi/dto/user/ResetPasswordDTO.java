package com.mvgore.walletapi.dto.user;

import jakarta.validation.constraints.NotBlank;

public class ResetPasswordDTO {

    @NotBlank
    private String email;

    @NotBlank
    private String newPassword;

    public ResetPasswordDTO() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}