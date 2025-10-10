package com.deodev.transactionService.pspservice.dto.request;

import lombok.Builder;

@Builder
public record ClientChargeRequest(
        String client
) {
}
