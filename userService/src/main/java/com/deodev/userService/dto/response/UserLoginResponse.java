package com.deodev.userService.dto.response;

import lombok.Builder;

@Builder
public record UserLoginResponse(
        String accessToken,
        String refreshToken,
        String user
) {
}
