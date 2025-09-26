package com.deodev.transactionService.client;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.request.ReserveFundsRequest;
import com.deodev.transactionService.dto.response.ReserveFundsResponse;
import com.deodev.transactionService.dto.response.ValidateWalletPinResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

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
        UUID reservationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ApiResponse<ReserveFundsResponse> expectedResponse = ApiResponse.success(
                HttpStatus.OK.value(), ReserveFundsResponse.builder().fundReservationId(reservationId).build());

        stubFor(post(urlEqualTo("/api/v1/wallets/accounts/funds/reserve"))
                .withHeader("X-User-Id", equalTo(userId.toString()))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(expectedResponse))));

        // when
        ApiResponse<ReserveFundsResponse> actualResponse =
                walletServiceClient.reserveFunds(ReserveFundsRequest.builder().build(), userId.toString());

        // then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(actualResponse.isSuccess()).isTrue();
        assertThat(actualResponse.getData().fundReservationId()).isEqualTo(reservationId);
    }
}