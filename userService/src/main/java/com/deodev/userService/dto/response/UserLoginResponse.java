package com.deodev.userService.dto.response;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
public record UserLoginResponse(
        boolean isSuccess,
        HttpStatus statusCode,
        LocalDateTime timestamp,
        String token,
        String user
) {
}
