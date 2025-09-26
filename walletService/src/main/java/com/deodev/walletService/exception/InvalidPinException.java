package com.deodev.walletService.exception;

import com.deodev.walletService.enums.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidPinException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.INVALID_PIN;
    public InvalidPinException(String message) {
        super(message);
    }
    public InvalidPinException(String message, Throwable cause) {
        super(message, cause);
    }
    public InvalidPinException(Throwable cause) {
        super(cause);
    }
}
