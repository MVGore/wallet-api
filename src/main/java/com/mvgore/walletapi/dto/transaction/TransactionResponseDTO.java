package com.mvgore.walletapi.dto.transaction;

import com.mvgore.walletapi.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponseDTO {

    private BigDecimal amount;
    private TransactionType type;
    private LocalDateTime timestamp;
    private String email;
    private String description;
    private String referenceId;

    public TransactionResponseDTO(BigDecimal amount,
                                  TransactionType type,
                                  LocalDateTime timestamp,
                                  String email,
                                  String description,
                                  String referenceId) {
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.email = email;
        this.description = description;
        this.referenceId = referenceId;
    }

    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getEmail() { return email; }
    public String getDescription() { return description; }
    public String getReferenceId() { return referenceId; }
}