package com.deodev.userService.dto.response;

import com.deodev.userService.enums.ErrorCode;
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
}
