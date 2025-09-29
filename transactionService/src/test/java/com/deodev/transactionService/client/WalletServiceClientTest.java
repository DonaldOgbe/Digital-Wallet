package com.deodev.transactionService.client;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.request.ClientP2PTransferRequest;
import com.deodev.transactionService.dto.response.ClientP2PTransferResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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


    private ClientP2PTransferRequest request;
    private ClientP2PTransferResponse response;
    private String sender;
    private String receiver;
    private Long amount;
    private UUID transactionId;
    private String pin;
    private UUID fundReservationId;

    @BeforeEach
    void setup() {
        sender = "0123456789";
        receiver = "1111111111";
        amount = 100L;
        transactionId = UUID.randomUUID();
        fundReservationId = UUID.randomUUID();
        pin = "1234";

        request = ClientP2PTransferRequest.builder()
                .senderAccountNumber(sender).receiverAccountNumber(receiver)
                .amount(amount).pin(pin).transactionId(transactionId).build();

        response = ClientP2PTransferResponse.builder()
                .transactionId(transactionId).fundReservationId(fundReservationId)
                .amount(amount).senderAccountNumber(sender).receiverAccountNumber(receiver).build();
    }


    @Test
    void shouldSendTransferFundsRequestAndParseResponse() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        ResponseEntity<ApiResponse<?>> expectedResponse = ResponseEntity.status(HttpStatus.OK.value())
                .body(ApiResponse.success(HttpStatus.OK.value(), response));

        stubFor(post(urlEqualTo("/api/v1/wallets/accounts/funds/p2p/transfer"))
                .withHeader("X-User-Id", equalTo(userId.toString()))
                .withRequestBody(equalTo(mapper.writeValueAsString(request)))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(expectedResponse.getBody()))));

        // when
        ResponseEntity<ApiResponse<?>> actualResponse =
                walletServiceClient.p2pTransfer(request, userId.toString());

        ClientP2PTransferResponse body = mapper.convertValue(actualResponse.getBody().getData(), ClientP2PTransferResponse.class);

        // then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResponse.getBody().isSuccess()).isTrue();
        assertThat(body.transactionId()).isEqualTo(transactionId);
    }
}