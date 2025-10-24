package com.deodev.transactionService.pspService.flutterwave.dto.response;

import lombok.Builder;

@Builder
public record FilteredVerifyChargeCardResponse(
        String status,
        String message,
        int id,
        String txn_ref,
        String flw_ref,
        String processor_response,
        String data_status
) {
}
