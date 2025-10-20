package com.deodev.transactionService.transactionService.service;

import com.deodev.transactionService.exception.ResourceNotFoundException;
import com.deodev.transactionService.pspService.walletService.client.WalletServiceClient;
import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.enums.Currency;
import com.deodev.transactionService.enums.ErrorCode;
import com.deodev.transactionService.enums.TransactionStatus;
import com.deodev.transactionService.exception.ExternalServiceException;
import com.deodev.transactionService.dto.request.ClientP2PTransferRequest;
import com.deodev.transactionService.transactionService.dto.request.P2PTransferRequest;
import com.deodev.transactionService.transactionService.dto.response.P2PTransferCompletedResponse;
import com.deodev.transactionService.transactionService.model.CardFundingTransaction;
import com.deodev.transactionService.transactionService.model.P2PTransaction;
import com.deodev.transactionService.transactionService.model.Transaction;
import com.deodev.transactionService.transactionService.repository.CardFundingTransactionRepository;
import com.deodev.transactionService.transactionService.repository.P2PTransactionRepository;
import com.deodev.transactionService.transactionService.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final P2PTransactionRepository p2PTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final CardFundingTransactionRepository cardFundingTransactionRepository;
    private final WalletServiceClient walletServiceClient;
    private final ObjectMapper mapper;

    // Transaction
    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public Transaction getTransaction(UUID id) {
            return transactionRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Transaction not Found by id: "+ id));
    }

    // Card Funding Transaction
    public CardFundingTransaction saveCardFundingTransaction(CardFundingTransaction cardFundingTransaction) {
        return cardFundingTransactionRepository.save(cardFundingTransaction);
    }

    public CardFundingTransaction getCardFundingTransaction(UUID id) {
        return cardFundingTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card Funding Transaction not Found by id: "+ id));
    }

    @Transactional
    public void setFailedCardFundingTransaction(CardFundingTransaction cardFundingTransaction,
                                                Transaction transaction, ErrorCode errorCode) {
        cardFundingTransaction.setStatus(TransactionStatus.FAILED);
        saveCardFundingTransaction(cardFundingTransaction);
        transaction.setErrorCode(errorCode);
        transaction.setStatus(TransactionStatus.FAILED);
        saveTransaction(transaction);
    }

    @Transactional
    public void setSuccessfulCardFundingTransaction(CardFundingTransaction cardFundingTransaction,
                                                    Transaction transaction) {
        cardFundingTransaction.setStatus(TransactionStatus.SUCCESSFUL);
        transaction.setStatus(TransactionStatus.SUCCESSFUL);
        saveCardFundingTransaction(cardFundingTransaction);
        saveTransaction(transaction);
    }

    public P2PTransaction createNewP2PTransaction(String sender, String receiver, Long amount, Currency currency) {
        P2PTransaction p2PTransaction = P2PTransaction.builder()
                .senderAccountNumber(sender)
                .receiverAccountNumber(receiver)
                .amount(amount)
                .currency(currency)
                .status(TransactionStatus.PENDING)
                .build();

        return p2PTransactionRepository.save(p2PTransaction);
    }

    public ApiResponse<?> processP2PTransfer(P2PTransferRequest request, String userId, Currency currency) {
        P2PTransaction p2PTransaction = createNewP2PTransaction(request.senderAccountNumber(), request.receiverAccountNumber(),
                request.amount(), currency);

        try {
            ApiResponse<?> response = getP2PTransferResponseFromClient(request, userId, p2PTransaction.getId());

            return processP2PTransferResponse(response, p2PTransaction);
        } catch (Exception e) {
            setFailedP2PTransaction(p2PTransaction, ErrorCode.P2P_TRANSFER_ERROR);
            log.error("Transaction {} failed due to exception", p2PTransaction.getId(), e);
            throw e;
        }
    }

    void setFailedP2PTransaction(P2PTransaction p2PTransaction, ErrorCode errorCode) {
        p2PTransaction.setErrorCode(errorCode);
        p2PTransaction.setStatus(TransactionStatus.FAILED);
        p2PTransactionRepository.save(p2PTransaction);
    }

    void setSuccessfulTransaction(P2PTransaction p2PTransaction) {
        p2PTransaction.setStatus(TransactionStatus.SUCCESSFUL);
        p2PTransactionRepository.save(p2PTransaction);
    }

    ApiResponse<?> getP2PTransferResponseFromClient(P2PTransferRequest request, String userId, UUID transactionId) {
        try {
            return walletServiceClient.p2pTransfer(ClientP2PTransferRequest.builder()
                            .senderAccountNumber(request.senderAccountNumber())
                            .receiverAccountNumber(request.receiverAccountNumber())
                            .amount(request.amount())
                            .pin(request.pin())
                            .transactionId(transactionId)
                            .build(),
                    userId).getBody();
        } catch (Exception e) {
            log.error("WalletServiceClient Error", e);
            throw new ExternalServiceException(e.getMessage(), e);
        }
    }

    ApiResponse<?> processP2PTransferResponse(ApiResponse<?> response, P2PTransaction p2PTransaction) {
        if (!response.isSuccess()) {
            setFailedP2PTransaction(p2PTransaction, response.getErrorCode());
            return response;
        }

        setSuccessfulTransaction(p2PTransaction);

        return ApiResponse.success(HttpStatus.OK.value(), P2PTransferCompletedResponse.builder()
                .transactionId(p2PTransaction.getId())
                .senderAccountNumber(p2PTransaction.getSenderAccountNumber())
                .receiverAccountNumber(p2PTransaction.getReceiverAccountNumber())
                .amount(p2PTransaction.getAmount())
                .currency(p2PTransaction.getCurrency())
                .status(p2PTransaction.getStatus())
                .timestamp(LocalDateTime.now())
                .build());
    }


}
