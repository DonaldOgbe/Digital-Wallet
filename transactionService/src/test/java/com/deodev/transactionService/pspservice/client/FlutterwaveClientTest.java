package com.deodev.transactionService.pspservice.client;

import com.deodev.transactionService.pspservice.dto.request.ClientChargeRequest;
import com.deodev.transactionService.pspservice.dto.request.OtpValidateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(properties = {
        "psp.flutterwave.url=http://localhost:${wiremock.server.port}",
        "psp.flutterwave.secret-key=FLWSECK_TEST-12345"
})
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
class FlutterwaveClientTest {
    @Autowired
    private FlutterwaveClient flutterwaveClient;

    @Autowired
    private ObjectMapper mapper;

    private String bin;
    private ClientChargeRequest clientChargeRequest;
    private String chargeCardJsonResponse;
    private String resolveCardJsonResponse;

    @BeforeEach
    void setup() {
        bin = "564000";

        resolveCardJsonResponse = """
                {
                  "status": "success",
                  "message": "BIN resolved successfully",
                  "data": {
                    "card_type": "MASTERCARD",
                    "issuer": "GTBank"
                  }
                }
                """;

        clientChargeRequest = new ClientChargeRequest("encrypted");

        chargeCardJsonResponse = """
                {
                  "status": "success",
                  "message": "Charge initiated",
                  "data": {
                    "id": 288192886,
                    "tx_ref": "LiveCardTest",
                    "flw_ref": "YemiDesola/FLW275389391",
                    "amount": 100,
                    "currency": "NGN",
                    "status": "pending",
                    "payment_type": "card",
                    "customer": {
                      "id": 216517630,
                      "name": "Yemi Desola",
                      "email": "usef@gmail.com"
                    },
                    "card": {
                      "first_6digits": "123456",
                      "last_4digits": "2343",
                      "issuer": "MASTERCARD GUARANTY TRUST BANK Mastercard Naira Debit Card",
                      "country": "NG",
                      "type": "MASTERCARD",
                      "expiry": "08/22"
                    }
                  },
                  "meta": {
                    "authorization": {
                      "mode": "pin",
                      "fields": ["pin"]
                    }
                  }
                }
                """;
    }

    @Test
    void shouldResolveCardSuccessfully() throws Exception {
        // given
        stubFor(get(urlEqualTo("/v3/card-bins/" + bin))
                .withHeader("Authorization", equalTo("Bearer FLWSECK_TEST-12345"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(resolveCardJsonResponse)));

        // when
        Map<String, Object> response = flutterwaveClient.resolveCard(bin);

        // then
        assertThat(response.get("status")).isEqualTo("success");

        Map<String, Object> data = (Map<String, Object>) response.get("data");
        assertThat(data.get("card_type")).isEqualTo("MASTERCARD");
        assertThat(data.get("issuer")).isEqualTo("GTBank");
    }

    @Test
    void shouldChargeCardSuccessfully_WhenFlutterwaveReturnsSuccessResponse() throws IOException {
        // given
        stubFor(post(urlEqualTo("/v3/charges?type=card"))
                .withHeader("Authorization", matching("Bearer FLWSECK_TEST-12345"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalTo(mapper.writeValueAsString(clientChargeRequest)))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(chargeCardJsonResponse)));

        // when
        Map<String, Object> response = flutterwaveClient.chargeCard(clientChargeRequest);

        // then
        assertThat(response.get("status")).isEqualTo("success");

        Map<String, Object> data = (Map<String, Object>) response.get("data");
        assertThat(data.get("tx_ref")).isEqualTo("LiveCardTest");
        assertThat(data.get("status")).isEqualTo("pending");

        verify(postRequestedFor(urlEqualTo("/v3/charges?type=card"))
                .withHeader("Authorization", matching("Bearer FLWSECK_TEST-12345"))
                .withHeader("Accept", equalTo("application/json")));
    }

    @Test
    void validateCharge_shouldReturnSuccessfulResponse() throws Exception {
        // given
        OtpValidateRequest request = OtpValidateRequest.builder()
                .otp("123456")
                .flw_ref("FLW247999960")
                .type("card")
                .build();

        String requestBody = mapper.writeValueAsString(request);

        String validateChargeJsonResponse = """
            {
              "status": "success",
              "message": "Charge validated",
              "data": {
                "id": 288200108,
                "tx_ref": "LiveCardTest",
                "flw_ref": "FLW275407301",
                "device_fingerprint": "N/A",
                "amount": 100,
                "charged_amount": 100,
                "app_fee": 1.4,
                "merchant_fee": 0,
                "processor_response": "Approved by Financial Institution",
                "auth_model": "PIN",
                "currency": "NGN",
                "ip": "::ffff:10.5.179.3",
                "narration": "CARD Transaction ",
                "status": "successful",
                "auth_url": "N/A",
                "payment_type": "card",
                "fraud_status": "ok",
                "charge_type": "normal",
                "created_at": "2020-07-15T14:31:16.000Z",
                "account_id": 17321,
                "customer": {
                  "id": 216519823,
                  "phone_number": null,
                  "name": "Yemi Desola",
                  "email": "user@gmail.com",
                  "created_at": "2020-07-15T14:31:15.000Z"
                },
                "card": {
                  "first_6digits": "232343",
                  "last_4digits": "4567",
                  "issuer": "VERVE FIRST CITY MONUMENT BANK PLC",
                  "country": "NG",
                  "type": "VERVE",
                  "expiry": "03/23"
                }
              }
            }
        """;

        stubFor(post(urlEqualTo("/v3/validate-charge"))
                .withHeader("Authorization", matching("Bearer FLWSECK_TEST-12345"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(validateChargeJsonResponse)));

        // when
        Map<String, Object> response = flutterwaveClient.validateCharge(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.get("status")).isEqualTo("success");
        assertThat(response.get("message")).isEqualTo("Charge validated");

        verify(postRequestedFor(urlEqualTo("/v3/validate-charge"))
                .withHeader("Authorization", matching("Bearer FLWSECK_TEST-12345"))
                .withRequestBody(equalToJson(requestBody)));
    }

    @Test
    void verifyCharge_shouldReturnSuccessfulResponse() {
        // given
        String transactionId = "288200108";

        String verifyChargeJsonResponse = """
            {
              "status": "success",
              "message": "Transaction fetched successfully",
              "data": {
                "id": 288200108,
                "tx_ref": "LiveCardTest",
                "flw_ref": "YemiDesola/FLW275407301",
                "device_fingerprint": "N/A",
                "amount": 100,
                "currency": "NGN",
                "charged_amount": 100,
                "app_fee": 1.4,
                "merchant_fee": 0,
                "processor_response": "Approved by Financial Institution",
                "auth_model": "PIN",
                "ip": "::ffff:10.5.179.3",
                "narration": "CARD Transaction ",
                "status": "successful",
                "payment_type": "card",
                "created_at": "2020-07-15T14:31:16.000Z",
                "account_id": 17321,
                "card": {
                  "first_6digits": "232343",
                  "last_4digits": "4567",
                  "issuer": "FIRST CITY MONUMENT BANK PLC",
                  "country": "NIGERIA NG",
                  "type": "VERVE",
                  "token": "flw-t1nf-4676a40c7ddf5f12scr432aa12d471973-k3n",
                  "expiry": "02/23"
                },
                "meta": null,
                "amount_settled": 98.6,
                "customer": {
                  "id": 216519823,
                  "name": "Yemi Desola",
                  "phone_number": "N/A",
                  "email": "user@gmail.com",
                  "created_at": "2020-07-15T14:31:15.000Z"
                }
              }
            }
        """;

        stubFor(get(urlEqualTo("/v3/transactions/" + transactionId + "/verify"))
                .withHeader("Authorization", matching("Bearer FLWSECK_TEST-12345"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(verifyChargeJsonResponse)));

        // when
        Map<String, Object> response = flutterwaveClient.verifyCharge(transactionId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.get("status")).isEqualTo("success");
        assertThat(response.get("message")).isEqualTo("Transaction fetched successfully");

        verify(getRequestedFor(urlEqualTo("/v3/transactions/" + transactionId + "/verify"))
                .withHeader("Authorization", matching("Bearer FLWSECK_TEST-12345")));
    }
}