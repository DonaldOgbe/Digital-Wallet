package com.deodev.userService.client;

import com.deodev.userService.dto.request.CreateWalletRequest;
import com.deodev.userService.dto.response.CreateWalletResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.util.UUID;


@SpringBootTest
@ActiveProfiles("test")
class WalletServiceClientTest {

    @Autowired
    private WalletServiceClient walletServiceClient;
    private static MockWebServer mockWebServer;
    private final ObjectMapper mapper = new ObjectMapper();

    @DynamicPropertySource
    public static void modifyProperty(DynamicPropertyRegistry registry) {
        try {
            mockWebServer = new MockWebServer();
            mockWebServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        registry.add("wallet.service.url", () -> mockWebServer.url("/").toString());
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (mockWebServer != null)
            mockWebServer.close();
    }

    @Test
    void createWallet() throws JsonProcessingException {
        // given
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        CreateWalletResponse expectedResponse = CreateWalletResponse
                .builder()
                .success(true)
                .note("Wallet created successfully")
                .userId(userId)
                .walletId(walletId).build();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(expectedResponse)));

        // when
        CreateWalletRequest request = CreateWalletRequest
                .builder()
                .userId(userId)
                .build();

        CreateWalletResponse actualResponse = walletServiceClient.createWallet(request,
                "Bearer fake-json-web-token");

        // then
        assertThat(expectedResponse).isEqualTo(actualResponse);
    }
}