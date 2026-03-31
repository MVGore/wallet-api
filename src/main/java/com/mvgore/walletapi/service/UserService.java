package com.mvgore.walletapi.service;

import com.mvgore.walletapi.dto.user.*;
import com.mvgore.walletapi.entity.User;
import com.mvgore.walletapi.exception.ResourceNotFoundException;
import com.mvgore.walletapi.repository.UserRepository;
import com.mvgore.walletapi.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponseDTO getCurrentUser() {
        CustomUserDetails userDetails = (CustomUserDetails)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserMapper.toDTO(user);
    }

    public void changePassword(ChangePasswordRequestDTO dto) {

        CustomUserDetails userDetails = (CustomUserDetails)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    public void forgotPassword(ForgotPasswordRequestDTO dto) {

        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not registered"));
    }

    public void resetPassword(ResetPasswordDTO dto) {

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}