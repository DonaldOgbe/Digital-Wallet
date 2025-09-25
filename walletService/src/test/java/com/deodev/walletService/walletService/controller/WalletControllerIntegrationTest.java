package com.deodev.walletService.walletService.controller;

import com.deodev.walletService.rabbitmq.publisher.WalletEventsPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class WalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletEventsPublisher walletEventsPublisher;

    @Test
    void createWallet_ReturnsCreated_WhenValidTokenAndUserId() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);

        // when & then
        mockMvc.perform(post("/api/v1/wallets")
                        .header("X-User-Id", userId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.data.walletId").isNotEmpty())
                .andExpect(jsonPath("$.data.userId").value(userId.toString()));

        verify(walletEventsPublisher, times(1)).publishWalletCreated(captor.capture());
        assertThat(captor.getValue()).isEqualTo(userId);
    }
}