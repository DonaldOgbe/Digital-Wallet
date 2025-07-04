package com.deodev.userService.dto.response;

import lombok.*;
import org.springframework.http.HttpStatus;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    @Builder.Default
    private boolean success = true;

    private String message;

    @Builder.Default
    private HttpStatus status = HttpStatus.OK;

    private T data;

    private String error;
}
