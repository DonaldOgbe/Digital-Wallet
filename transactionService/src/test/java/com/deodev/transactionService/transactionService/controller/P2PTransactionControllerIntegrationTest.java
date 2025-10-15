package com.deodev.transactionService.transactionService.controller;

import com.deodev.transactionService.pspService.walletService.client.WalletServiceClient;
import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.enums.Currency;
import com.deodev.transactionService.enums.TransactionStatus;
import com.deodev.transactionService.redis.RedisCacheService;
import com.deodev.transactionService.transactionService.dto.request.P2PTransferRequest;
import com.deodev.transactionService.transactionService.dto.response.P2PTransferCompletedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class P2PTransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WalletServiceClient walletServiceClient;

    @MockBean
    private RedisCacheService redisCacheService;

    private UUID userId;
    private String sender;
    private String receiver;
    private String pin;
    private Long amount;
    private Currency currency;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        sender = "1234567890";
        receiver = "0987654321";
        pin = "1234";
        amount = 1000L;
        currency = Currency.NGN;
    }

    @Test
    void p2pTransfer_ShouldReturnSuccess_WhenServiceReturnsSuccess() throws Exception {
        // given
        P2PTransferRequest request = new P2PTransferRequest(sender, receiver, amount, pin);

        ResponseEntity<ApiResponse<?>> clientResponse = ResponseEntity.status(HttpStatus.OK.value())
                .body(ApiResponse.success(HttpStatus.OK.value(), "success"));

        when(redisCacheService.getCacheResponse(any())).thenReturn(null);
        when(walletServiceClient.p2pTransfer(any(), any())).thenReturn(clientResponse);

        // when & then
        mockMvc.perform(post("/api/v1/transactions/transfer/p2p")
                        .header("X-User-Id", userId.toString())
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .param("currency", "NGN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionId").isNotEmpty())
                .andExpect(jsonPath("$.data.senderAccountNumber").value(sender))
                .andExpect(jsonPath("$.data.receiverAccountNumber").value(receiver))
                .andExpect(jsonPath("$.data.amount").value(1000))
                .andExpect(jsonPath("$.data.currency").value(currency.toString()))
                .andExpect(jsonPath("$.data.status").value(TransactionStatus.SUCCESSFUL.toString()));
    }

    @Test
    void p2pTransfer_ShouldReturnCachedResponse_WhenCacheExists() throws Exception {
        // given
        UUID transactionId = UUID.randomUUID();
        P2PTransferRequest request = new P2PTransferRequest(sender, receiver, amount, pin);

        P2PTransferCompletedResponse completed = P2PTransferCompletedResponse.builder()
                .transactionId(transactionId)
                .senderAccountNumber("1234567890")
                .receiverAccountNumber("0987654321")
                .amount(500L)
                .currency(Currency.NGN)
                .status(TransactionStatus.SUCCESSFUL)
                .timestamp(LocalDateTime.now())
                .build();

        ApiResponse<?> cachedResponse = ApiResponse.success(HttpStatus.OK.value(), completed);
        String cachedJson = objectMapper.writeValueAsString(cachedResponse);

        String idempotencyKey = "idem-123";

        when(redisCacheService.getCacheResponse("p2pTransfer:" + idempotencyKey))
                .thenReturn(cachedJson);

        // when & then
        mockMvc.perform(post("/api/v1/transactions/transfer/p2p")
                        .header("X-User-Id", "test-user")
                        .header("Idempotency-Key", idempotencyKey)
                        .param("currency", "NGN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionId").value(transactionId.toString()))
                .andExpect(jsonPath("$.data.senderAccountNumber").value("1234567890"))
                .andExpect(jsonPath("$.data.receiverAccountNumber").value("0987654321"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }
}