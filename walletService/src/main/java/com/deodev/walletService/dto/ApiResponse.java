package com.deodev.walletService.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    @Builder.Default
    private boolean success = true;

    private String message;

    private String note;

    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String note, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .note(note)
                .data(data)
                .build();
    }
}
