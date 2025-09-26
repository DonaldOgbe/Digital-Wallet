package com.deodev.walletService.exception;

import com.deodev.walletService.enums.ErrorCode;
import lombok.Getter;

@Getter
public class FundReservationException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.FUND_RESERVATION_ERROR;
    public FundReservationException(String message) {
        super(message);
    }
    public FundReservationException(String message, Throwable cause) {
        super(message, cause);
    }
    public FundReservationException(Throwable cause) {
        super(cause);
    }
}
