package com.mvgore.walletapi.repository;

import com.mvgore.walletapi.entity.Transaction;
import com.mvgore.walletapi.entity.TransactionType;
import com.mvgore.walletapi.entity.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByWallet(Wallet wallet, Pageable pageable);

    Page<Transaction> findByWalletAndType(Wallet wallet, TransactionType type, Pageable pageable);

    List<Transaction> findByWallet(Wallet wallet, Sort sort);

    @Query("SELECT t FROM Transaction t WHERE t.wallet = :wallet AND " +
            "(LOWER(t.wallet.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.wallet.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.referenceId) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.type) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Transaction> searchByWalletAndKeyword(@Param("wallet") Wallet wallet,
                                               @Param("keyword") String keyword,
                                               Pageable pageable);

    Page<Transaction> findByWalletAndTypeIn(Wallet wallet, List<TransactionType> types, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE " +
            "LOWER(t.wallet.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.wallet.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.referenceId) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.type) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Transaction> searchAllTransactions(@Param("keyword") String keyword,
                                            Pageable pageable);

    Page<Transaction> findByTypeIn(List<TransactionType> types, Pageable pageable);
}