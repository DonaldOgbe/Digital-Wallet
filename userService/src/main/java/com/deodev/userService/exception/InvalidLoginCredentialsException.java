package com.deodev.userService.exception;

public class InvalidLoginCredentialsException extends RuntimeException {
    public InvalidLoginCredentialsException(String message) {
        super(message);
    }
    public InvalidLoginCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
    public InvalidLoginCredentialsException(Throwable cause) {
        super(cause);
    }
}
