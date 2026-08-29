package com.altech.walletledger.exception;

public class PurchaseNotPayableException extends RuntimeException {

    public PurchaseNotPayableException() {
        super("Purchase cannot be completed");
    }
}
