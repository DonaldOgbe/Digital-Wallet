package com.deodev.transactionService.client;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.response.ValidateWalletPinResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = {"wallet.service.url=http://localhost:${wiremock.server.port}"})
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
class WalletServiceClientTest {
    @Autowired
    private WalletServiceClient walletServiceClient;
    @Autowired
    private ObjectMapper mapper;


    @Test
    void shouldSendTransferFundsRequestAndParseResponse() throws Exception {
        // given
        ApiResponse<ValidateWalletPinResponse> expectedResponse = ApiResponse.success(
                HttpStatus.OK.value(), ValidateWalletPinResponse.builder().isValid(true).build());

        stubFor(post(urlEqualTo("/api/v1/wallets/pin/validate"))
                .withHeader("Authorization", equalTo("Bearer token"))
                .withHeader("Wallet-Pin", equalTo("1234"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(expectedResponse))));

        // when
        ApiResponse<ValidateWalletPinResponse> actualResponse =
                walletServiceClient.validatePin("Bearer token", "1234");

        // then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(actualResponse.isSuccess()).isTrue();
        assertThat(actualResponse.getData().isValid()).isTrue();
    }
}