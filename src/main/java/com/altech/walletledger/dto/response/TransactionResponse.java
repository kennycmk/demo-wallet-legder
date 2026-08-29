package com.altech.walletledger.dto.response;

import com.altech.walletledger.enums.EntryType;
import com.altech.walletledger.enums.TransactionReason;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID userId,
        EntryType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        TransactionReason reason,
        Instant createdAt
) {
}
