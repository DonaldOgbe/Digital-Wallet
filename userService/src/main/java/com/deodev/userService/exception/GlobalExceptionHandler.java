package com.deodev.userService.exception;

import com.deodev.userService.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleUserExists(UserAlreadyExistsException e) {
        return handleResponse(HttpStatus.BAD_REQUEST,
                "Registration Failed",
                e.getMessage(),
                null
        );
    }

    @ExceptionHandler(InvalidLoginCredentialsException.class)
    public ResponseEntity<?> handleInvalidLoginCredentials(InvalidLoginCredentialsException e) {
        return handleResponse(HttpStatus.BAD_REQUEST,
                "Login Failed",
                e.getMessage(),
                null
                );
    }

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<?> handleTokenValidationError(TokenValidationException e) {
        return handleResponse(HttpStatus.UNAUTHORIZED,
                "Token Validation Error",
                e.getMessage(),
                null);
    }

    private <T> ResponseEntity<ApiResponse<T>>  handleResponse(HttpStatus status, String message, String error, T data) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .status(status)
                .success(false)
                .message(message)
                .error(error)
                .data(data)
                .build();

        return ResponseEntity.status(response.getStatus())
                .body(response);
    }
}
