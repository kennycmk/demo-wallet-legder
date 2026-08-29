package com.altech.walletledger.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WalletResponse(
        UUID userId,
        BigDecimal balance,
        Instant updatedAt,
        TransactionResponse transaction
) {
}
