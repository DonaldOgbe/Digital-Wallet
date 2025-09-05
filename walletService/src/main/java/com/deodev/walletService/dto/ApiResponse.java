package com.deodev.walletService.dto;

import com.deodev.walletService.enums.ErrorCode;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    @Builder.Default
    private boolean success = true;

    private int statusCode;

    @Builder.Default
    private ErrorCode errorCode = null;

    private T data;

    public static <T> ApiResponse<T> success(int statusCode, T data) {
        return ApiResponse.<T>builder()
                .statusCode(statusCode)
                .data(data)
                .build();
    }
}