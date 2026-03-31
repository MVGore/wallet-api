package com.mvgore.walletapi.dto.transaction;

import com.mvgore.walletapi.entity.Transaction;

public class TransactionMapper {

    public static TransactionResponseDTO toDTO(Transaction tx) {

        String email = null;

        if (tx.getWallet() != null && tx.getWallet().getUser() != null) {
            email = tx.getWallet().getUser().getEmail();
        }

        return new TransactionResponseDTO(
                tx.getAmount(),
                tx.getType(),
                tx.getTimestamp(),
                email,
                tx.getDescription(),
                tx.getReferenceId()
        );
    }
}