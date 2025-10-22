package com.deodev.transactionService.pspService.flutterwave.dto.response;

import com.deodev.transactionService.enums.Currency;
import lombok.Builder;

@Builder
public record VerifyChargeCardResponse(
        String status,
        String message,
        Long id,
        String txn_ref,
        String flw_ref,
        String transactionId,
        Long amount,
        Currency currency
) {
}
