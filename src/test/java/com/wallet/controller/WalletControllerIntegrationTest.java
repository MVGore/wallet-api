package com.wallet.controller;

import com.wallet.dto.WalletOperationRequest;
import com.wallet.entity.Wallet;
import com.wallet.repository.WalletRepository;
import com.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(locations = "classpath:application-test.yml")
class WalletControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("wallet_db_test")
            .withUsername("wallet_user")
            .withPassword("wallet_password");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    private UUID testWalletId;

    @BeforeEach
    void setUp() {
        testWalletId = UUID.randomUUID();
        walletRepository.deleteAll();
        walletService.createWallet(testWalletId, BigDecimal.valueOf(1000));
    }

    @Test
    void testDepositOperation() throws Exception {
        WalletOperationRequest request = WalletOperationRequest.builder()
                .walletId(testWalletId)
                .operationType(WalletOperationRequest.OperationType.DEPOSIT)
                .amount(BigDecimal.valueOf(500))
                .build();

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId", is(testWalletId.toString())))
                .andExpect(jsonPath("$.balance", is(1500.0)));
    }

    @Test
    void testWithdrawOperation() throws Exception {
        WalletOperationRequest request = WalletOperationRequest.builder()
                .walletId(testWalletId)
                .operationType(WalletOperationRequest.OperationType.WITHDRAW)
                .amount(BigDecimal.valueOf(300))
                .build();

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId", is(testWalletId.toString())))
                .andExpect(jsonPath("$.balance", is(700.0)));
    }

    @Test
    void testWithdrawInsufficientFunds() throws Exception {
        WalletOperationRequest request = WalletOperationRequest.builder()
                .walletId(testWalletId)
                .operationType(WalletOperationRequest.OperationType.WITHDRAW)
                .amount(BigDecimal.valueOf(2000))
                .build();

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INSUFFICIENT_FUNDS")))
                .andExpect(jsonPath("$.message", containsString("Insufficient funds")));
    }

    @Test
    void testWalletNotFound() throws Exception {
        UUID nonExistentWalletId = UUID.randomUUID();
        WalletOperationRequest request = WalletOperationRequest.builder()
                .walletId(nonExistentWalletId)
                .operationType(WalletOperationRequest.OperationType.DEPOSIT)
                .amount(BigDecimal.valueOf(100))
                .build();

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("WALLET_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Wallet not found")));
    }

    @Test
    void testGetWalletBalance() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/" + testWalletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId", is(testWalletId.toString())))
                .andExpect(jsonPath("$.balance", is(1000.0)));
    }

    @Test
    void testGetWalletBalanceNotFound() throws Exception {
        UUID nonExistentWalletId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/wallets/" + nonExistentWalletId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("WALLET_NOT_FOUND")));
    }

    @Test
    void testInvalidRequestFormat() throws Exception {
        String invalidRequest = "{\"walletId\": null, \"operationType\": \"DEPOSIT\", \"amount\": 100}";

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.errors", hasKey("walletId")));
    }

    @Test
    void testInvalidAmount() throws Exception {
        WalletOperationRequest request = WalletOperationRequest.builder()
                .walletId(testWalletId)
                .operationType(WalletOperationRequest.OperationType.DEPOSIT)
                .amount(BigDecimal.valueOf(-100))
                .build();

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
    }
}
