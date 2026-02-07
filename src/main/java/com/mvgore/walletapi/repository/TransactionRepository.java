package com.mvgore.walletapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mvgore.walletapi.entity.*;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}
