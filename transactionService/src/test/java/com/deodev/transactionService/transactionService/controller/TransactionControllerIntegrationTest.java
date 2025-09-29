package com.deodev.transactionService.transactionService.controller;

import com.deodev.transactionService.client.WalletServiceClient;
import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.request.ClientP2PTransferRequest;
import com.deodev.transactionService.enums.Currency;
import com.deodev.transactionService.enums.TransactionStatus;
import com.deodev.transactionService.transactionService.dto.request.P2PTransferRequest;
import com.deodev.transactionService.transactionService.dto.response.P2PTransferCompletedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WalletServiceClient walletServiceClient;

    @Test
    void p2pTransfer_ShouldReturnSuccess_WhenServiceReturnsSuccess() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String sender = "1234567890";
        String receiver = "0987654321";
        String pin = "1234";
        Long amount = 1000L;
        Currency currency = Currency.NGN;

        P2PTransferRequest request = new P2PTransferRequest(sender, receiver, amount, pin);

        ResponseEntity<ApiResponse<?>> clientResponse = ResponseEntity.status(HttpStatus.OK.value())
                .body(ApiResponse.success(HttpStatus.OK.value(), "success"));


        when(walletServiceClient.p2pTransfer(any(), any())).thenReturn(clientResponse);

        // when & then
        mockMvc.perform(post("/api/v1/transactions/transfer/p2p")
                        .header("X-User-Id", userId.toString())
                        .param("currency", "NGN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionId").isNotEmpty())
                .andExpect(jsonPath("$.data.senderAccountNumber").value(sender))
                .andExpect(jsonPath("$.data.receiverAccountNumber").value(receiver))
                .andExpect(jsonPath("$.data.amount").value(1000))
                .andExpect(jsonPath("$.data.currency").value(currency.toString()))
                .andExpect(jsonPath("$.data.status").value(TransactionStatus.COMPLETED.toString()));
    }
}