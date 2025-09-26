package com.deodev.walletService.exception;

import com.deodev.walletService.enums.ErrorCode;
import lombok.Getter;

@Getter
public class ExternalServiceException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.EXTERNAL_SERVICE_ERROR;
    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExternalServiceException(Throwable cause) {
        super(cause);
    }
}
