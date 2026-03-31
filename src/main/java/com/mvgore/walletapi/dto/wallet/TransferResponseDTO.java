package com.mvgore.walletapi.dto.wallet;

import java.math.BigDecimal;

public class TransferResponseDTO {

    private String senderEmail;
    private BigDecimal senderBalance;

    private String receiverEmail;
    private BigDecimal receiverBalance;

    private String referenceId;

    public TransferResponseDTO(String senderEmail, BigDecimal senderBalance,
                               String receiverEmail, BigDecimal receiverBalance,
                               String referenceId) {
        this.senderEmail = senderEmail;
        this.senderBalance = senderBalance;
        this.receiverEmail = receiverEmail;
        this.receiverBalance = receiverBalance;
        this.referenceId = referenceId;
    }

    public String getSenderEmail() { return senderEmail; }
    public BigDecimal getSenderBalance() { return senderBalance; }

    public String getReceiverEmail() { return receiverEmail; }
    public BigDecimal getReceiverBalance() { return receiverBalance; }

    public String getReferenceId() { return referenceId; }
}