package com.deodev.apiGateway.dto;

import lombok.Builder;

@Builder
public record ErrorResponse(
        String message,
        String path
) {
}
