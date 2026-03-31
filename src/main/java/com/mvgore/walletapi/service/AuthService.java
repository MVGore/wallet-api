package com.mvgore.walletapi.service;

import com.mvgore.walletapi.dto.user.*;
import com.mvgore.walletapi.entity.User;
import com.mvgore.walletapi.entity.Wallet;
import com.mvgore.walletapi.repository.UserRepository;
import com.mvgore.walletapi.repository.WalletRepository;
import com.mvgore.walletapi.security.CustomUserDetails;
import com.mvgore.walletapi.security.JwtUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, WalletRepository walletRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public RegisterResponseDTO register(RegisterRequestDTO request) {

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }


        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        user = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        walletRepository.save(wallet);

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getTokenVersion()
        );

        return new RegisterResponseDTO(
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                token
        );
    }

    public LoginResponseDTO login(LoginRequestDTO request) {

        if (request.getUsernameOrEmail() == null || request.getUsernameOrEmail().isBlank()) {
            throw new IllegalArgumentException("Username or Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        User user;
        if (request.getUsernameOrEmail().contains("@")) {
            user = userRepository.findByEmail(request.getUsernameOrEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Email not registered"));
        } else {
            user = Optional.ofNullable(userRepository.findByUsername(request.getUsernameOrEmail()))
                    .orElseThrow(() -> new IllegalArgumentException("Username not found"));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getTokenVersion()
        );

        return new LoginResponseDTO(
                token,
                user.getUsername(),
                user.getRole()
        );
    }

    public void logout() {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated())
            throw new IllegalArgumentException("User not authenticated");

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }
}