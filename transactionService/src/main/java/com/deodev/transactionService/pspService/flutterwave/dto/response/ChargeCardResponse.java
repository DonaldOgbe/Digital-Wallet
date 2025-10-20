package com.deodev.transactionService.pspService.flutterwave.dto.response;

import lombok.Builder;


@Builder
public record ChargeCardResponse(
        String txn_ref,
        Long id,
        String flw_ref,
        String mode,
        String redirect,
        String message

) {
}
