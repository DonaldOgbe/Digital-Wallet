package com.deodev.transactionService.exception;

public class PSPException extends RuntimeException {
    public PSPException(String message) {
        super(message);
    }

    public PSPException(String message, Throwable cause) {
        super(message, cause);
    }

    public PSPException(Throwable cause) {
        super(cause);
    }
}
