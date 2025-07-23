package com.deodev.userService.exception;

public class ServiceClientUnavailableException extends RuntimeException {
    public ServiceClientUnavailableException(String message) {
        super(message);
    }
}
