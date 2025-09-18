package com.deodev.walletService.walletPinService.dto.response;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
public record ValidateWalletPinResponse(
        boolean isValid
) {
}
