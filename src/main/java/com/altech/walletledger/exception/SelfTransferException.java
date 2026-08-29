package com.altech.walletledger.exception;

public class SelfTransferException extends RuntimeException {

    public SelfTransferException() {
        super("Cannot transfer to yourself");
    }
}
