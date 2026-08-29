package com.altech.walletledger.exception;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class InsufficientFundsException extends RuntimeException {

    private final UUID userId;
    private final BigDecimal balance;
    private final BigDecimal attempted;

    public InsufficientFundsException(UUID userId, BigDecimal balance, BigDecimal attempted) {
        super("Insufficient funds for user " + userId);
        this.userId = userId;
        this.balance = balance;
        this.attempted = attempted;
    }
}
