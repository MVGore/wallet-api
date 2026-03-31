package com.mvgore.walletapi.dto.user;

import com.mvgore.walletapi.entity.User;

public class UserMapper {
    public static UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}