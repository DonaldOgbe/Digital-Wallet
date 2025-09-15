package com.deodev.userService.exception;

import com.deodev.userService.dto.response.ApiResponse;
import com.deodev.userService.dto.response.ErrorResponse;
import com.deodev.userService.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

@ControllerAdvice
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
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_REQUEST,
                errors,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<?> handleExternalServiceException(ExternalServiceException e, HttpServletRequest request) {
        logger.error(e.getMessage(), e);
        return handleResponse(
                HttpStatus.FAILED_DEPENDENCY,
                ErrorCode.EXTERNAL_SERVICE_ERROR,
                e.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidLoginCredentialsException.class)
    public ResponseEntity<?> handleInvalidLoginCredentials(InvalidLoginCredentialsException e, HttpServletRequest request) {
        logger.error(e.getMessage(), e);
        return handleResponse(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.INVALID_CREDENTIALS,
                e.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        logger.error(e.getMessage(), e);
        return handleResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.NOT_FOUND,
                e.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<?> handleTokenValidation(TokenValidationException e, HttpServletRequest request) {
        logger.error(e.getMessage(), e);
        return handleResponse(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.INVALID_TOKEN,
                e.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleUserAlreadyExists(UserAlreadyExistsException e, HttpServletRequest request) {
        logger.error(e.getMessage(), e);
        return handleResponse(
                HttpStatus.CONFLICT,
                ErrorCode.USER_ALREADY_EXISTS,
                e.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleExceptions(Exception e, HttpServletRequest request) {
        logger.error("Global Exception", e);
        return handleResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.SYSTEM_ERROR,
                e.getMessage(),
                request.getRequestURI()
        );
    }

    private ResponseEntity<ApiResponse<ErrorResponse>> handleResponse(
            HttpStatus statusCode,
            ErrorCode errorCode,
            String message,
            String path
    ) {
        return ResponseEntity.status(statusCode).body(ApiResponse.<ErrorResponse>builder()
                .success(false)
                .statusCode(statusCode.value())
                .errorCode(errorCode)
                .data(ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .statusCode(statusCode)
                        .errorCode(errorCode)
                        .message(message)
                        .path(path)
                        .build())
                .build());
    }
}
