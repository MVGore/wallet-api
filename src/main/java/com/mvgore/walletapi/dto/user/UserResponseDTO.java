package com.mvgore.walletapi.dto.user;

import com.mvgore.walletapi.entity.User;

public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String role;

    public UserResponseDTO() {}

    public UserResponseDTO(Long id, String username, String email, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    public String getUsername() { return username; }  // ✅ corrected from getName()
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}