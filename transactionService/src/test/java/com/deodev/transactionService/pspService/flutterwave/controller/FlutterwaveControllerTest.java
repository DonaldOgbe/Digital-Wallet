package com.deodev.transactionService.pspService.flutterwave.controller;

import com.deodev.transactionService.pspService.flutterwave.client.FlutterwaveClient;
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
import java.util.Map;

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
        mockMvc.perform(get("/api/v1/psp/flutterwave/card-type/{bin}", bin)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data.cardType").value("MASTERCARD"));
    }
}