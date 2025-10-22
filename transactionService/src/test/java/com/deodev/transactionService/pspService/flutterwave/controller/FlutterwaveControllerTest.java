package com.deodev.transactionService.pspService.flutterwave.controller;

import com.deodev.transactionService.enums.*;
import com.deodev.transactionService.pspService.flutterwave.client.FlutterwaveClient;
import com.deodev.transactionService.pspService.flutterwave.dto.request.InitiateChargeCardRequest;
import com.deodev.transactionService.pspService.flutterwave.dto.request.VerifyChargeCardRequest;
import com.deodev.transactionService.pspService.walletService.service.WalletService;
import com.deodev.transactionService.redis.RedisCacheService;
import com.deodev.transactionService.transactionService.model.CardFundingTransaction;
import com.deodev.transactionService.transactionService.model.Transaction;
import com.deodev.transactionService.transactionService.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class FlutterwaveControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TransactionService transactionService;

    @MockBean
    private FlutterwaveClient flutterwaveClient;

    @MockBean
    private WalletService walletService;

    @MockBean
    private RedisCacheService redisCacheService;

    private String bin;
    private Map<String, Object> flutterwaveResponse;
    private UUID userId;
    private String accountNumber;

    @BeforeEach
    void setup() {
        bin = "564000";
        flutterwaveResponse = new HashMap<>();
        flutterwaveResponse.put("status", "success");

        Map<String, Object> data = new HashMap<>();
        data.put("card_type", "MASTERCARD");
        data.put("issuer", "GTBank");
        flutterwaveResponse.put("data", data);

        accountNumber = "0123456789";
        userId = UUID.randomUUID();
    }

    @Test
    void shouldReturnCardType_WhenFlutterwaveClientReturnsSuccess() throws Exception {
        // given
        when(flutterwaveClient.resolveCard(anyString())).thenReturn(flutterwaveResponse);

        // when & then
        mockMvc.perform(get("/api/v1/psp/flutterwave/card/card-type/{bin}", bin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data.cardType").value("MASTERCARD"));
    }

    @Test
    void shouldReturnPinModeResponse_WhenFlutterwaveClientReturnsSuccess_AndPinAuthMode() throws Exception {
        // given
        UUID txn_ref = UUID.randomUUID();

        InitiateChargeCardRequest request = InitiateChargeCardRequest.builder()
                .currency(Currency.USD)
                .amount(1000L)
                .txn_ref(txn_ref.toString())
                .client("encrypted")
                .accountNumber("0123456789")
                .cardType(CardType.MASTERCARD)
                .cardLast4("5569")
                .build();

        Map<String, Object> flutterwaveResponse = Map.of(
                "status", "success",
                "message", "Charge initiated",
                "meta", Map.of(
                        "authorization", Map.of(
                                "mode", "pin",
                                "fields", List.of("pin")
                        )));

        when(redisCacheService.getCacheResponse(any())).thenReturn(null);
        when(walletService.verifyAccountNumber(anyString(), any())).thenReturn(true);

        when(flutterwaveClient.chargeCard(any())).thenReturn(flutterwaveResponse);

        // when
        mockMvc.perform(post("/api/v1/psp/flutterwave/card/initiate-card-funding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());

    }

    @Test
    void shouldReturnSuccessResponse_WhenVerifyCardFundingIsSuccessful() throws Exception {
        // given
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.CARD_FUND)
                .userId(userId)
                .accountNumber(accountNumber)
                .amount(1000L)
                .currency(Currency.NGN)
                .status(TransactionStatus.PENDING)
                .idempotencyKey("12345")
                .build();

        Transaction savedTransaction = transactionService.saveTransaction(transaction);

        CardFundingTransaction cardFundingTransaction =  CardFundingTransaction.builder()
                .id(UUID.randomUUID())
                .transactionId(savedTransaction.getId())
                .accountNumber(accountNumber)
                .status(TransactionStatus.PENDING)
                .paymentGateway(PaymentGateway.FLUTTERWAVE)
                .build();

        transactionService.saveCardFundingTransaction(cardFundingTransaction);

        String idempotencyKey = UUID.randomUUID().toString();

        VerifyChargeCardRequest request = VerifyChargeCardRequest.builder()
                .id(288192886L)
                .txn_ref(cardFundingTransaction.getId().toString())
                .build();

        Map<String, Object> flutterwaveResponse = Map.of(
                "status", "success",
                "message", "",
                "data", Map.of(
                        "id", 288192886,
                        "txn_ref", cardFundingTransaction.getId().toString(),
                        "flw_ref", "FLW-455DJ",
                        "processor_response", "Approved Successfully",
                        "status", "successful"
                ));

        when(redisCacheService.getCacheResponse("flw_verify_card_funding:" + idempotencyKey))
                .thenReturn(null);

        when(flutterwaveClient.verifyCharge(any())).thenReturn(flutterwaveResponse);

        // when
        mockMvc.perform(post("/api/v1/psp/flutterwave/card/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", idempotencyKey)
                        .content(new ObjectMapper().writeValueAsString(request)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("successful"))
                .andExpect(jsonPath("$.data.message").value("Approved Successfully"))
                .andExpect(jsonPath("$.data.currency").value("NGN"))
                .andExpect(jsonPath("$.data.id").value(288192886));

        // verify cache interactions
        verify(redisCacheService).getCacheResponse("flw_verify_card_funding:" + idempotencyKey);
        verify(redisCacheService).cacheResponse(eq("flw_verify_card_funding:" + idempotencyKey), anyString());

        Transaction updated = transactionService.getTransaction(savedTransaction.getId());

        assertThat(updated.getStatus()).isEqualTo(TransactionStatus.SUCCESSFUL);
    }

}