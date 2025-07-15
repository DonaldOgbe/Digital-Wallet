package com.deodev.walletService.exception;
import com.deodev.walletService.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
