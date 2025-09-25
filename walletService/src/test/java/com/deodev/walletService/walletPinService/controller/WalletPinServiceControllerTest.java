package com.deodev.walletService.walletPinService.controller;

import com.deodev.walletService.walletPinService.dto.request.SetPinRequest;
import com.deodev.walletService.walletPinService.dto.request.UpdatePinRequest;
import com.deodev.walletService.walletPinService.model.WalletPin;
import com.deodev.walletService.walletPinService.repository.WalletPinRepository;
import com.deodev.walletService.walletService.model.Wallet;
import com.deodev.walletService.walletService.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WalletPinServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletPinRepository walletPinRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID userId;

    @BeforeEach
    void setUp() {
        walletPinRepository.deleteAll();
        userId = UUID.randomUUID();
    }

    @Test
    void setNewPin_ShouldReturn201_WhenRequestIsValid() throws Exception {
        // given
        walletRepository.save(Wallet.builder()
                .userId(userId)
                .build());

        SetPinRequest request = new SetPinRequest("1234", "1234");

        // when & then
        mockMvc.perform(post("/api/v1/wallets/pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userId").value(String.valueOf(userId)))
                .andExpect(jsonPath("$.data.walletId").isNotEmpty())
                .andExpect(jsonPath("$.data.walletPinId").isNotEmpty());
    }

    @Test
    void updatePin_ShouldReturn200_WhenRequestIsValid() throws Exception {
        // given
        walletPinRepository.save(WalletPin.builder()
                .walletId(UUID.randomUUID())
                .userId(userId)
                .pin(passwordEncoder.encode("1234"))
                .build());

        UpdatePinRequest request = new UpdatePinRequest("1234", "5678", "5678");

        // when & then
        mockMvc.perform(patch("/api/v1/wallets/pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(String.valueOf(userId)))
                .andExpect(jsonPath("$.data.walletPinId").isNotEmpty());
    }

}