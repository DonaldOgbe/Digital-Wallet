package com.deodev.userService.dto.response;

import com.deodev.userService.enums.UserStatus;
import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserRegisteredResponse(
        UUID userId,
        String email,
        String accessToken,
        String refreshToken
) {
}
