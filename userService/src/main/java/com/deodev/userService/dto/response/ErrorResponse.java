package com.deodev.userService.dto.response;

import lombok.Builder;

@Builder
public record ErrorResponse(
        String message,
        String path
) {
}
