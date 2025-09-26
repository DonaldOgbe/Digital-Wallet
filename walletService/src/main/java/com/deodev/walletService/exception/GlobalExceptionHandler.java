package com.deodev.walletService.exception;

import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.ErrorResponse;
import com.deodev.walletService.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_REQUEST,
                errors,
                request.getRequestURI()
        );
    }


    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<?> handlePinMismatchException(InvalidPinException e, HttpServletRequest request) {
        logger.error(e.getMessage(), e);
        return handleResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.PIN_MISMATCH,
                e.getMessage(),
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

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        logger.error(e.getMessage(), e);
        return handleResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.NOT_FOUND,
                e.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(FundReservationException.class)
    public ResponseEntity<?> handleFundReservationException(FundReservationException e, HttpServletRequest request) {
        logger.error(e.getMessage(), e);
        return handleResponse(
                HttpStatus.CONFLICT,
                ErrorCode.FUND_RESERVATION_ERROR,
                e.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(P2PTransferException.class)
    public ResponseEntity<?> handleP2PTransferException(P2PTransferException e, HttpServletRequest request) {
        logger.error(e.getMessage(), e);
        return handleResponse(
                HttpStatus.CONFLICT,
                ErrorCode.P2P_TRANSFER_ERROR,
                e.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleExceptions(Exception e, HttpServletRequest request) {

        logger.error("Global Exception", e);
        return handleResponse(
                HttpStatus.BAD_REQUEST,
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
                        .message(message)
                        .path(path)
                        .build())
                .build());
    }
}
