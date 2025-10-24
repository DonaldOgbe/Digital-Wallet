package com.deodev.walletService.dto;

import com.deodev.walletService.enums.ErrorCode;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    @Builder.Default
    private boolean success = true;

    private int statusCode;

    private LocalDateTime timestamp;

    @Builder.Default
    private ErrorCode errorCode = null;

    private T data;

    public static <T> ApiResponse<T> success(int statusCode, T data) {
        return ApiResponse.<T>builder()
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int statusCode, ErrorCode errorCode, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .errorCode(errorCode)
                .data(data)
                .build();
    }
}