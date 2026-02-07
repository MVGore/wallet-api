package com.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;  // Added to clear security context after tests
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.mvgore.walletapi.auth.UserRepository;
import com.mvgore.walletapi.repository.WalletRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.mvgore.walletapi.WalletApplication.class)
@AutoConfigureMockMvc
public class WalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;  // Injecting UserRepository

    @Autowired
    private WalletRepository walletRepository;  // Injecting WalletRepository

    @BeforeEach
    void setUp() throws Exception {
        // Clear the database to ensure each test starts with a clean state
        userRepository.deleteAll();  // Make sure the userRepo is correctly initialized
        walletRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // Any cleanup after each test (e.g., clear context or reset state)
    }

    @Test
    void testUserRegistration() throws Exception {
        String registerJson = """
                {
                    "username": "testuser",
                    "password": "password"
                }
                """;

        // First registration attempt should succeed
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));

        // Second registration attempt with the same username should fail
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void testUserLogin() throws Exception {
        String registerJson = """
                {
                    "username": "testuser",
                    "password": "password"
                }
                """;

        // Register user first
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));

        // Now, try to login with the registered credentials
        String loginJson = """
                {
                    "username": "testuser",
                    "password": "password"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
