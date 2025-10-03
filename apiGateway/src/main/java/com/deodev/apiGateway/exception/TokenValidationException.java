package com.deodev.apiGateway.exception;

import com.deodev.apiGateway.enums.ErrorCode;
import lombok.Getter;

@Getter
public class TokenValidationException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.INVALID_TOKEN;
    public TokenValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenValidationException(String message) {
        super(message);
    }

    public TokenValidationException(Throwable cause) {
        super(cause);
    }
}
