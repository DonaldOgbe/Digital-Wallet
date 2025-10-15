package com.deodev.transactionService.transactionService.service;

import com.deodev.transactionService.pspService.walletService.client.WalletServiceClient;
import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.request.ClientP2PTransferRequest;
import com.deodev.transactionService.dto.response.ClientP2PTransferResponse;
import com.deodev.transactionService.enums.Currency;
import com.deodev.transactionService.enums.ErrorCode;
import com.deodev.transactionService.enums.TransactionStatus;
import com.deodev.transactionService.transactionService.dto.request.P2PTransferRequest;
import com.deodev.transactionService.transactionService.model.P2PTransaction;
import com.deodev.transactionService.transactionService.repository.P2PTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class P2PTransactionServiceTest {

    @Mock
    private P2PTransactionRepository p2PTransactionRepository;

    @Mock
    private WalletServiceClient walletServiceClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void getP2PTransferResponseFromClient_ShouldReturnResponse_WhenClientSucceeds() {
        // given
        UUID transactionId = UUID.randomUUID();
        ClientP2PTransferRequest request = ClientP2PTransferRequest.builder()
                .senderAccountNumber("1234567890")
                .receiverAccountNumber("0987654321")
                .amount(100L)
                .pin("1234")
                .transactionId(transactionId)
                .build();

        P2PTransferRequest p2pRequest = P2PTransferRequest.builder()
                .senderAccountNumber("1234567890")
                .receiverAccountNumber("0987654321")
                .amount(100L)
                .pin("1234")
                .build();

        String userId = "user-123";
        ApiResponse<String> expectedResponse = ApiResponse.success(200, "OK");

        when(walletServiceClient.p2pTransfer(eq(request), eq(userId)))
                .thenReturn(ResponseEntity.ok(expectedResponse));

        // when
        ApiResponse<?> result = transactionService.getP2PTransferResponseFromClient(p2pRequest, userId, transactionId);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo("OK");

        verify(walletServiceClient).p2pTransfer(request, userId);
    }

    @Test
    void processP2PTransferResponse_ShouldMarkFailed_WhenResponseIsNotSuccess() {
        // given
        P2PTransaction p2PTransaction = P2PTransaction.builder()
                .senderAccountNumber("1234567890")
                .receiverAccountNumber("0987654321")
                .amount(200L)
                .currency(Currency.USD)
                .status(TransactionStatus.PENDING)
                .build();

        ApiResponse<?> failedResponse = ApiResponse.error(HttpStatus.CONFLICT.value(), ErrorCode.INSUFFICIENT_FUNDS, null);

        // when
        ApiResponse<?> result = transactionService.processP2PTransferResponse(failedResponse, p2PTransaction);

        // then
        assertThat(result.isSuccess()).isFalse();
        assertThat(p2PTransaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(p2PTransaction.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_FUNDS);

        verify(p2PTransactionRepository).save(p2PTransaction);
    }

    @Test
    void processP2PTransferResponse_ShouldMarkSuccessful_WhenResponseIsSuccess() {
        // given
        P2PTransaction p2PTransaction = P2PTransaction.builder()
                .senderAccountNumber("1234567890")
                .receiverAccountNumber("0987654321")
                .amount(500L)
                .currency(Currency.NGN)
                .status(TransactionStatus.PENDING)
                .build();

        ClientP2PTransferResponse mockResponseData = ClientP2PTransferResponse.builder()
                .transactionId(p2PTransaction.getId())
                .build();

        ApiResponse<?> successResponse = ApiResponse.success(200, mockResponseData);

        when(objectMapper.convertValue(successResponse.getData(), ClientP2PTransferResponse.class))
                .thenReturn(mockResponseData);

        // when
        ApiResponse<?> result = transactionService.processP2PTransferResponse(successResponse, p2PTransaction);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(p2PTransaction.getStatus()).isEqualTo(TransactionStatus.SUCCESSFUL);

        verify(p2PTransactionRepository).save(p2PTransaction);
        verify(objectMapper).convertValue(successResponse.getData(), ClientP2PTransferResponse.class);
    }

}