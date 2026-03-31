package com.mvgore.walletapi.entity;

public enum TransactionType {
    ADMIN_CREDIT,
    ADMIN_DEBIT,
    TRANSFER_DEBIT,   // money sent
    TRANSFER_CREDIT,
    SELF_CREDIT,
    SELF_DEBIT
}