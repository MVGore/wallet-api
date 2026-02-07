package com.mvgore.walletapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Object, Long> {
}
