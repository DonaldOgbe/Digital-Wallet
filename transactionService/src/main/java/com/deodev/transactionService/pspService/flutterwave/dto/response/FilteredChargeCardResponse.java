package com.deodev.transactionService.pspService.flutterwave.dto.response;

import lombok.Builder;

import java.util.Map;

@Builder
public record FilteredChargeCardResponse(
        Long id,
        String txn_ref,
        String flw_ref,
        String processor_response,
        String status,
        String mode,
        Map<String, Object> authorization
) {
}
