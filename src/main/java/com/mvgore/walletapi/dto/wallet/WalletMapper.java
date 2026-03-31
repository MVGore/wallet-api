package com.mvgore.walletapi.dto.wallet;

import com.mvgore.walletapi.entity.Wallet;

public class WalletMapper {

    public static Wallet toEntity(WalletRequestDTO dto) {
        Wallet wallet = new Wallet();
        wallet.setBalance(dto.getAmount());
        return wallet;
    }

    public static WalletResponseDTO toDTO(Wallet wallet) {

        if (wallet.getUser() == null) {
            throw new RuntimeException("Wallet has no associated user");
        }

        return new WalletResponseDTO(
                wallet.getId(),
                wallet.getUser().getId(),
                wallet.getBalance(),
                wallet.getUser().getUsername(),
                wallet.getUser().getEmail(),
                wallet.isFrozen()
        );
    }
}