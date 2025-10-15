package com.deodev.transactionService.pspService.flutterwave.dto.request;

import com.deodev.transactionService.enums.CardType;
import com.deodev.transactionService.enums.Currency;
import lombok.Builder;

@Builder
public record InitiateChargeCardRequest(
        Currency currency,
        Long amount,
        String txn_ref,
        String client,
        String accountNumber,
        CardType cardType,
        String cardLast4
) {
}
