package com.altech.walletledger.dto.response;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String email
) {
}
