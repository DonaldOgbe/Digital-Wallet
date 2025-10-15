package com.deodev.transactionService.pspService.flutterwave.dto.response;

import lombok.Builder;


@Builder
public record InitiateChargeCardResponse(
        String txn_ref,
        String id,
        String flw_ref,
        String mode,
        String redirect,
        String message

) {
}
