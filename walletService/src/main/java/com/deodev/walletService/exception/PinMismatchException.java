package com.deodev.walletService.exception;

public class PinMismatchException extends RuntimeException {
    public PinMismatchException(String message) {
        super(message);
    }
}
