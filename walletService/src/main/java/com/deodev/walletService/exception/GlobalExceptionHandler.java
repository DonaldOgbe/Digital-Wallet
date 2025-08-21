package com.deodev.walletService.exception;
import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationError(MethodArgumentNotValidException e, HttpServletRequest request) {
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
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                errors,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAuthorizationDeniedExceptions(AccessDeniedException e, HttpServletRequest request) {
        return handleResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN,
                "Authorization Error",
                "Access Denied",
                request.getRequestURI()
        );
    }


    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<?> handleTokenValidationExceptions(TokenValidationException e, HttpServletRequest request) {

        return handleResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED,
                "Token Validation Error",
                e.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(PinMismatchException.class)
    public ResponseEntity<?> handlePinMismatchException(PinMismatchException e, HttpServletRequest request) {
        return handleResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST,
                "PIN Mismatch Error",
                e.getMessage(),
                request.getRequestURI()
        );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleExceptions(Exception e, HttpServletRequest request) {

        logger.error("Global Exception", e);
        return handleResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST,
                "Global Error",
                e.getMessage(),
                request.getRequestURI()
        );
    }

    private ResponseEntity<ErrorResponse> handleResponse(
            LocalDateTime timestamp,
            HttpStatus status,
            String error,
            String message,
            String path
    ) {
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.builder()
                        .timestamp(timestamp)
                        .status(status)
                        .error(error)
                        .message(message)
                        .path(path)
                        .build());
    }
}
