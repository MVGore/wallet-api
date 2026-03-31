package com.mvgore.walletapi.dto.wallet;

import java.math.BigDecimal;

public class WalletResponseDTO {

    private Long id;          // ✅ FIXED (was walletId)
    private Long userId;
    private BigDecimal balance;
    private String username;
    private String email;
    private boolean frozen;

    public WalletResponseDTO(Long id, Long userId, BigDecimal balance,
                             String username, String email, boolean frozen) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
        this.username = username;
        this.email = email;
        this.frozen = frozen;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean isFrozen() {
        return frozen;
    }
}