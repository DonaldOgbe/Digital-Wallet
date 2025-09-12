package com.deodev.transactionService.client;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.TransferFundsResponse;
import com.deodev.transactionService.dto.request.TransferFundsRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class WalletServiceClientTest {
    @Autowired
    private WalletServiceClient walletServiceClient;
    private static MockWebServer mockWebServer;
    @Autowired
    private ObjectMapper mapper;

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
    void shouldSendTransferFundsRequestAndParseResponse() throws Exception {
        // given
        UUID transactionId = UUID.randomUUID();
        UUID fundReservationId = UUID.randomUUID();

        TransferFundsRequest request = TransferFundsRequest.builder()
                .accountNumber("1234567890")
                .transactionId(transactionId)
                .build();

        TransferFundsResponse response = TransferFundsResponse.builder()
                .isSuccess(true)
                .statusCode(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .transactionId(transactionId)
                .fundReservationId(fundReservationId)
                .build();

        ApiResponse<TransferFundsResponse> apiResponse = ApiResponse.<TransferFundsResponse>builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .data(response)
                .build();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(apiResponse))
                .setResponseCode(200));

        // when
        ApiResponse<TransferFundsResponse> actualResponse =
                walletServiceClient.transferFunds("Bearer dummyToken", request);

        // then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/wallets/accounts/funds/transfer");
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer dummyToken");

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.isSuccess()).isTrue();
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(actualResponse.getData()).isNotNull();
        assertThat(actualResponse.getData().transactionId()).isEqualTo(transactionId);
        assertThat(actualResponse.getData().fundReservationId()).isEqualTo(fundReservationId);
    }
}