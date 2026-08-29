package com.altech.walletledger.exception;

import java.util.UUID;

public class PurchaseNotFoundException extends RuntimeException {

    public PurchaseNotFoundException(UUID purchaseId) {
        super("Purchase not found: " + purchaseId);
    }
}
