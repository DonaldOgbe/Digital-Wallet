package com.deodev.walletService.exception;
import com.deodev.walletService.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Objects;
import java.util.stream.Collectors;


@RestControllerAdvice
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
                errors,
                "Validation Error",
                null,
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<?> handleAuthorizationDeniedExceptions(AuthorizationDeniedException e) {
        return handleResponse(
                "Access Denied",
                "Authorization Error",
                null,
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<?> handleTokenValidationExceptions(TokenValidationException e) {

        return handleResponse(
                e.getMessage(),
                "Token Validation Error",
                null,
                HttpStatus.UNAUTHORIZED
        );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleExceptions(Exception e) {

        logger.error("Global Exception", e);
        return handleResponse(
                "Operation Failed",
                e.getMessage(),
                null,
                HttpStatus.BAD_REQUEST
                );
    }

    private <T> ResponseEntity<ApiResponse<T>> handleResponse(
            String message,
            String error,
            T data,
            HttpStatus status) {

        ApiResponse<T> response = ApiResponse.<T>builder()
                .message(message)
                .error(error)
                .status(status)
                .data(data)
                .build();

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}
