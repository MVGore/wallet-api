package com.mvgore.walletapi.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    protected Wallet() {}

    public Wallet(BigDecimal balance, UUID userId) {
        this.balance = balance;
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public UUID getUserId() {
        return userId;
    }

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }
}
