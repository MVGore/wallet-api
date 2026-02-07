package com.mvgore.walletapi.repository;

import com.mvgore.walletapi.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    // ✅ Find wallet by the owner's userId
    Optional<Wallet> findByUserId(UUID userId);
}
