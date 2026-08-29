package com.altech.walletledger.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank
        @Email
        String recipientEmail,

        @NotNull
        @DecimalMin(value = "0.0001", inclusive = true)
        @Digits(integer = 15, fraction = 4)
        BigDecimal amount
) {
}
