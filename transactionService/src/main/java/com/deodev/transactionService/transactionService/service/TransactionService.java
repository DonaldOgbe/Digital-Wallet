package com.deodev.transactionService.transactionService.service;

import com.deodev.transactionService.client.WalletServiceClient;
import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.enums.Currency;
import com.deodev.transactionService.enums.ErrorCode;
import com.deodev.transactionService.enums.TransactionStatus;
import com.deodev.transactionService.exception.ExternalServiceException;
import com.deodev.transactionService.dto.request.P2PTransferRequest;
import com.deodev.transactionService.dto.response.P2PTransferResponse;
import com.deodev.transactionService.transactionService.dto.response.P2PTransferCompletedResponse;
import com.deodev.transactionService.transactionService.model.Transaction;
import com.deodev.transactionService.transactionService.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletServiceClient walletServiceClient;
    private final ObjectMapper mapper;

    public Transaction createNewTransaction(String sender, String receiver, Long amount, Currency currency) {
        Transaction transaction = Transaction.builder()
                .senderAccountNumber(sender)
                .receiverAccountNumber(receiver)
                .amount(amount)
                .currency(currency)
                .build();

        return transactionRepository.save(transaction);
    }

    public ApiResponse<?> processP2PTransfer(P2PTransferRequest request, String userId, Currency currency) {
        Transaction transaction = createNewTransaction(request.senderAccountNumber(), request.receiverAccountNumber(),
                request.amount(), currency);

        try {
            ApiResponse<?> response = getP2PTransferResponseFromClient(request, userId);

            if (!response.isSuccess()) {
                setFailedTransaction(transaction, response.getErrorCode());
                return response;
            }

            P2PTransferResponse data = mapper.convertValue(response.getData(), P2PTransferResponse.class);
            setCompletedTransaction(transaction);

            return ApiResponse.success(HttpStatus.OK.value(), P2PTransferCompletedResponse.builder()
                    .transactionId(transaction.getId())
                    .senderAccountNumber(transaction.getSenderAccountNumber())
                    .receiverAccountNumber(transaction.getReceiverAccountNumber())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .status(transaction.getStatus())
                    .timestamp(LocalDateTime.now())
                    .build());

        } catch (Exception e) {
            setFailedTransaction(transaction, ErrorCode.P2P_TRANSFER_ERROR);
            log.error("Transaction {} failed due to exception", transaction.getId(), e);
            throw e;
        }
    }

    void setFailedTransaction(Transaction transaction, ErrorCode errorCode) {
        transaction.setErrorCode(errorCode);
        transaction.setStatus(TransactionStatus.FAILED);
        transactionRepository.save(transaction);
    }

    void setCompletedTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);
    }

    ApiResponse<?> getP2PTransferResponseFromClient(P2PTransferRequest request, String userId) {
        try {
            return walletServiceClient.p2pTransfer(request, userId).getBody();
        } catch (Exception e) {
            log.error("WalletServiceClient Error", e);
            throw new ExternalServiceException(e.getMessage(), e);
        }
    }



}
