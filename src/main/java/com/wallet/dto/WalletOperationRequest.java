package com.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletOperationRequest {

    @NotNull(message = "Wallet ID cannot be null")
    private UUID walletId;

    @NotNull(message = "Operation type cannot be null")
    private OperationType operationType;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    public enum OperationType {
        DEPOSIT, WITHDRAW
    }
}
