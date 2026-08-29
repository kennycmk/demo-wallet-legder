package com.altech.walletledger.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class WalletNotFoundException extends RuntimeException {

    private final UUID userId;

    public WalletNotFoundException(UUID userId) {
        super("Wallet not found for user " + userId);
        this.userId = userId;
    }
}
