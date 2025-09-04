package com.deodev.walletService.walletPinService.dto.response;

import lombok.Builder;

@Builder
public record ValidateWalletPinResponse(
        boolean isValid,
        String errorCode
) {
}
