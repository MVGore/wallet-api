package com.mvgore.walletapi.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Existing
    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(name = "reference_id", nullable = false, updatable = false)
    private String referenceId;

    private LocalDateTime timestamp;

    @Version
    private Long version;

    @Column(name = "description")
    private String description;

    public Transaction() {}

    // getters & setters

    public Long getId() { return id; }

    public Wallet getWallet() { return wallet; }

    public void setWallet(Wallet wallet) { this.wallet = wallet; }

    public BigDecimal getAmount() { return amount; }

    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getType() { return type; }

    public void setType(TransactionType type) { this.type = type; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getReferenceId() { return referenceId; }

    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}