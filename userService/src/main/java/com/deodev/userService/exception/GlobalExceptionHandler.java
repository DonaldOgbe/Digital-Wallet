package com.deodev.userService.exception;

import com.deodev.userService.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationError(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error
                        .getField()
                        .concat(": ")
                        .concat(Objects.requireNonNull(error.getDefaultMessage())))
                .collect(Collectors.joining("; "));

        logger.error("Validation Error", e);
        return handleResponse(
                HttpStatus.BAD_REQUEST,
                errors,
                "Validation Error");
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleUserExists(UserAlreadyExistsException e) {

        logger.error("User Already Exists", e);
        return handleResponse(HttpStatus.BAD_REQUEST,
                "Registration Failed",
                e.getMessage());
    }

    @ExceptionHandler(InvalidLoginCredentialsException.class)
    public ResponseEntity<?> handleInvalidLoginCredentials(InvalidLoginCredentialsException e) {

        logger.error("Invalid Login Credentials", e);
        return handleResponse(HttpStatus.BAD_REQUEST,
                "Login Failed",
                e.getMessage());
    }

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<?> handleTokenValidationError(TokenValidationException e) {
        return handleResponse(HttpStatus.UNAUTHORIZED,
                "Token Validation Error",
                e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleVagueExceptions(Exception e) {

        logger.error("Exception", e);
        return handleResponse(HttpStatus.BAD_REQUEST,
                "Internal Server Error",
                e.getMessage());
    }

    private <T> ResponseEntity<ApiResponse<T>>  handleResponse(HttpStatus httpStatus, String message, String note) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .note(note)
                .data(null)
                .build();

        return ResponseEntity.status(httpStatus)
                .body(response);
    }
}
