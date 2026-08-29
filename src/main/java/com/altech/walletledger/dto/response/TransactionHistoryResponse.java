package com.altech.walletledger.dto.response;

import java.util.List;
import java.util.UUID;

public record TransactionHistoryResponse(
        UUID userId,
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<TransactionResponse> items
) {
}
