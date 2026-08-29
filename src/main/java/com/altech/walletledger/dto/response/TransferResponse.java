package com.altech.walletledger.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID fromUserId,
        UUID toUserId,
        BigDecimal amount,
        BigDecimal senderBalance,
        Instant createdAt
) {
}
