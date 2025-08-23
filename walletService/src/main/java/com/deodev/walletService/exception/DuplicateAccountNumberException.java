package com.deodev.walletService.exception;

public class DuplicateAccountNumberException extends RuntimeException {
    public DuplicateAccountNumberException(String message) {
        super(message);
    }

    public DuplicateAccountNumberException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateAccountNumberException(Throwable cause) {
        super(cause);
    }
}
