package com.deodev.transactionService.pspService.walletService.service;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.ErrorResponse;
import com.deodev.transactionService.dto.request.AccountExistsRequest;
import com.deodev.transactionService.dto.request.ClientP2PTransferRequest;
import com.deodev.transactionService.enums.Currency;
import com.deodev.transactionService.enums.ErrorCode;
import com.deodev.transactionService.enums.TransactionStatus;
import com.deodev.transactionService.enums.TransactionType;
import com.deodev.transactionService.exception.ExternalServiceException;
import com.deodev.transactionService.pspService.flutterwave.dto.request.InitiateChargeCardRequest;
import com.deodev.transactionService.pspService.walletService.client.WalletServiceClient;
import com.deodev.transactionService.transactionService.dto.request.P2PTransferRequest;
import com.deodev.transactionService.transactionService.dto.response.P2PTransferCompletedResponse;
import com.deodev.transactionService.transactionService.model.CardFundingTransaction;
import com.deodev.transactionService.transactionService.model.P2PTransaction;
import com.deodev.transactionService.transactionService.model.Transaction;
import com.deodev.transactionService.transactionService.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletServiceClient walletServiceClient;
    private final TransactionService transactionService;

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

    public ApiResponse<?> processP2PTransfer(P2PTransferRequest request, String userId, String idempotencyKey) {

        Transaction transaction = createNewTransaction(request, userId, TransactionType.P2P, idempotencyKey);
        P2PTransaction p2pTransaction = createNewP2PTransaction(request, transaction.getId());

        try {
            ApiResponse<?> response = getP2PTransferResponseFromClient(request, UUID.fromString(userId), p2pTransaction.getId());

            return processP2PTransferResponse(response, p2pTransaction, transaction);
        }catch (ExternalServiceException ex) {
            transactionService.setFailedP2PTransaction(p2pTransaction, transaction, ErrorCode.EXTERNAL_SERVICE_ERROR);
            log.error("Wallet service error [txnId={}, idKey={}]: {}", transaction.getId(), idempotencyKey, ex.getMessage(), ex);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.EXTERNAL_SERVICE_ERROR,
                    ErrorResponse.builder().message("Wallet service temporarily unavailable").build());
        } catch (Exception ex) {
            transactionService.setFailedP2PTransaction(p2pTransaction, transaction, ErrorCode.EXTERNAL_SERVICE_ERROR);
            log.error("Transaction {} failed due to exception", p2pTransaction.getId(), ex);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.EXTERNAL_SERVICE_ERROR,
                    ErrorResponse.builder().message("Internal processing error").build());
        }
    }

    ApiResponse<?> getP2PTransferResponseFromClient(P2PTransferRequest request, UUID userId, UUID transactionId) {
        try {
            return walletServiceClient.p2pTransfer(ClientP2PTransferRequest.builder()
                    .senderAccountNumber(request.senderAccountNumber())
                    .receiverAccountNumber(request.receiverAccountNumber())
                    .amount(request.amount())
                    .pin(request.pin())
                    .transactionId(transactionId)
                    .userId(userId)
                    .build()).getBody();
        } catch (Exception e) {
            log.error("WalletServiceClient Error", e);
            throw new ExternalServiceException(e.getMessage(), e);
        }
    }

    ApiResponse<?> processP2PTransferResponse(ApiResponse<?> response, P2PTransaction p2pTransaction, Transaction transaction) {
        if (!response.isSuccess()) {
            transactionService.setFailedP2PTransaction(p2pTransaction, transaction, response.getErrorCode());
            return response;
        }

        transactionService.setSuccessfulP2PTransaction(p2pTransaction, transaction);

        return ApiResponse.success(HttpStatus.OK.value(), P2PTransferCompletedResponse.builder()
                .transactionId(p2pTransaction.getId())
                .senderAccountNumber(p2pTransaction.getSenderAccountNumber())
                .receiverAccountNumber(p2pTransaction.getReceiverAccountNumber())
                .amount(p2pTransaction.getAmount())
                .currency(p2pTransaction.getCurrency())
                .status(p2pTransaction.getStatus())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @Transactional
    Transaction createNewTransaction(P2PTransferRequest request,
                                     String userId, TransactionType type,
                                     String idempotencyKey) {
        Transaction transaction = Transaction.builder()
                .transactionType(type)
                .userId(UUID.fromString(userId))
                .accountNumber(request.senderAccountNumber())
                .amount(request.amount())
                .currency(request.currency())
                .status(TransactionStatus.PENDING)
                .idempotencyKey(idempotencyKey)
                .build();

        return transactionService.saveTransaction(transaction);
    }

    @Transactional
    public P2PTransaction createNewP2PTransaction(P2PTransferRequest request, UUID transactionId) {
        P2PTransaction p2PTransaction = P2PTransaction.builder()
                .transactionId(transactionId)
                .senderAccountNumber(request.senderAccountNumber())
                .receiverAccountNumber(request.receiverAccountNumber())
                .amount(request.amount())
                .currency(request.currency())
                .status(TransactionStatus.PENDING)
                .build();

        return transactionService.saveP2PTransaction(p2PTransaction);
    }

}
