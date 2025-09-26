package com.deodev.walletService.exception;

import com.deodev.walletService.enums.ErrorCode;
import lombok.Getter;

@Getter
public class DuplicateAccountNumberException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.DUPLICATE_ACCOUNT_NUMBER;
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
