package com.deodev.transactionService.pspService.walletService.service;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.request.AccountExistsRequest;
import com.deodev.transactionService.enums.Currency;
import com.deodev.transactionService.exception.ExternalServiceException;
import com.deodev.transactionService.pspService.walletService.client.WalletServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletServiceClient walletServiceClient;

    public Boolean verifyAccountNumber(String accountNumber, Currency currency) {
        ApiResponse<?> response;
        try {
            response = walletServiceClient.verifyAccountNumber(AccountExistsRequest.builder()
                    .accountNumber(accountNumber).currency(currency).build()).getBody();
        } catch (Exception ex) {
            log.error("Unexpected Error while verifying account number: {}", accountNumber, ex);
            throw new ExternalServiceException("Unexpected Error while verifying account number: "+ accountNumber, ex);
        }

        if (!Objects.requireNonNull(response).isSuccess()) {
            log.warn("Error from Wallet Service while verifying account number: {}", accountNumber);
            throw new ExternalServiceException("Error from Wallet Service while verifying account number: "+ accountNumber);
        }
        return (Boolean) response.getData();
    }

}
