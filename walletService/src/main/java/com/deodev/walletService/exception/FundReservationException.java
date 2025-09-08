package com.deodev.walletService.exception;

public class FundReservationException extends RuntimeException {
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
