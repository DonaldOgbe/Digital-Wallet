package com.deodev.transactionService.pspService.flutterwave.controller;

import com.deodev.transactionService.enums.CardType;
import com.deodev.transactionService.enums.Currency;
import com.deodev.transactionService.pspService.flutterwave.client.FlutterwaveClient;
import com.deodev.transactionService.pspService.flutterwave.dto.request.InitiateChargeCardRequest;
import com.deodev.transactionService.pspService.walletService.service.WalletService;
import com.deodev.transactionService.redis.RedisCacheService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @MockBean
    private FlutterwaveClient flutterwaveClient;

    @MockBean
    private WalletService walletService;

    @MockBean
    private RedisCacheService redisCacheService;

    private String bin;
    private Map<String, Object> flutterwaveResponse;

    @BeforeEach
    void setup() {
        bin = "564000";
        flutterwaveResponse = new HashMap<>();
        flutterwaveResponse.put("status", "success");

        Map<String, Object> data = new HashMap<>();
        data.put("card_type", "MASTERCARD");
        data.put("issuer", "GTBank");
        flutterwaveResponse.put("data", data);
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
}