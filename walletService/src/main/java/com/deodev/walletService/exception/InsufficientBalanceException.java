package com.deodev.walletService.exception;

import com.deodev.walletService.enums.ErrorCode;
import lombok.Getter;

@Getter
public class InsufficientBalanceException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.INSUFFICIENT_FUNDS;
    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(Throwable cause) {
        super(cause);
    }
}
