package com.deodev.transactionService.pspService.walletService.service;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.ErrorResponse;
import com.deodev.transactionService.dto.request.ClientP2PTransferRequest;
import com.deodev.transactionService.dto.response.ClientP2PTransferResponse;
import com.deodev.transactionService.enums.Currency;
import com.deodev.transactionService.enums.ErrorCode;
import com.deodev.transactionService.enums.TransactionStatus;
import com.deodev.transactionService.enums.TransactionType;
import com.deodev.transactionService.pspService.walletService.client.WalletServiceClient;
import com.deodev.transactionService.transactionService.dto.request.P2PTransferRequest;
import com.deodev.transactionService.transactionService.model.P2PTransaction;
import com.deodev.transactionService.transactionService.model.Transaction;
import com.deodev.transactionService.transactionService.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletServiceClient walletServiceClient;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private WalletService walletService;


    @Test
    void getP2PTransferResponseFromClient_ShouldReturnResponse_WhenClientSucceeds() {
        // given
        UUID transactionId = UUID.randomUUID();

        ClientP2PTransferRequest request = mock(ClientP2PTransferRequest.class);

        P2PTransferRequest p2pRequest = mock(P2PTransferRequest.class);

        UUID userId = UUID.randomUUID();
        ApiResponse<String> expectedResponse = ApiResponse.success(200, "OK");

        when(walletServiceClient.p2pTransfer(any()))
                .thenReturn(ResponseEntity.ok(expectedResponse));

        // when
        ApiResponse<?> result = walletService.getP2PTransferResponseFromClient(p2pRequest, userId, transactionId);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo("OK");
    }

    @Test
    void processP2PTransferResponse_ShouldMarkFailed_WhenResponseIsNotSuccess() {
        // given
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.P2P)
                .userId(UUID.randomUUID())
                .accountNumber("1234567890")
                .amount(200L)
                .currency(Currency.USD)
                .status(TransactionStatus.PENDING)
                .idempotencyKey("12345")
                .build();

        P2PTransaction p2pTransaction = P2PTransaction.builder()
                .transactionId(UUID.randomUUID())
                .senderAccountNumber("1234567890")
                .receiverAccountNumber("0987654321")
                .amount(200L)
                .currency(Currency.USD)
                .status(TransactionStatus.PENDING)
                .build();

        ApiResponse<?> failedResponse = ApiResponse.error(HttpStatus.CONFLICT.value(), ErrorCode.INSUFFICIENT_FUNDS, ErrorResponse.builder()
                .message("Insufficient funds"));

        // when
        ApiResponse<?> result = walletService.processP2PTransferResponse(failedResponse, p2pTransaction, transaction);

        // then
        assertThat(result.isSuccess()).isFalse();
        verify(transactionService).setFailedP2PTransaction(p2pTransaction, transaction, failedResponse.getErrorCode());
    }

    @Test
    void processP2PTransferResponse_ShouldMarkSuccessful_WhenResponseIsSuccess() {
        // given
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.P2P)
                .userId(UUID.randomUUID())
                .accountNumber("1234567890")
                .amount(200L)
                .currency(Currency.USD)
                .status(TransactionStatus.PENDING)
                .idempotencyKey("12345")
                .build();

        P2PTransaction p2pTransaction = P2PTransaction.builder()
                .transactionId(UUID.randomUUID())
                .senderAccountNumber("1234567890")
                .receiverAccountNumber("0987654321")
                .amount(200L)
                .currency(Currency.USD)
                .status(TransactionStatus.PENDING)
                .build();

        ApiResponse<?> successResponse = ApiResponse.success(200, "success");

        // when
        ApiResponse<?> result = walletService.processP2PTransferResponse(successResponse, p2pTransaction, transaction);

        // then
        assertThat(result.isSuccess()).isTrue();
        verify(transactionService).setSuccessfulP2PTransaction(p2pTransaction, transaction);
    }
}