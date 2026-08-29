package com.altech.walletledger.dto.response;

import com.altech.walletledger.enums.PurchaseStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PurchaseResponse(
        UUID purchaseId,
        BigDecimal amount,
        PurchaseStatus status,
        BigDecimal walletBalance,
        Instant createdAt
) {
}
