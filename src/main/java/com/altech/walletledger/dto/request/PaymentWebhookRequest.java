package com.altech.walletledger.dto.request;

import com.altech.walletledger.enums.PaymentEvent;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PaymentWebhookRequest(
        @NotNull
        UUID purchaseId,

        @NotNull
        PaymentEvent event
) {
}
