package com.deodev.walletService.client;

import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.response.GetUserDetailsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(
        properties = {
                "user.service.url=http://localhost:${wiremock.server.port}"
        }
)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
class UserServiceClientTest {
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private ObjectMapper mapper;

    @Test
    void getUser_ShouldReturnUserDetailsResponse() throws IOException {
        // given
        UUID userId = UUID.randomUUID();

        ApiResponse<GetUserDetailsResponse> expectedResponse = ApiResponse.success(
                HttpStatus.OK.value(),
                GetUserDetailsResponse.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .email("johndoe@email.com")
                        .build());

        stubFor(get(urlEqualTo("/api/v1/users/%s".formatted(userId)))
                .withHeader("Authorization", equalTo("Bearer token"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(expectedResponse))));

        // when
        ApiResponse<GetUserDetailsResponse> actualResponse = userServiceClient.getUser(userId, "Bearer token");

        // then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(actualResponse.isSuccess()).isTrue();
        assertThat(actualResponse.getData().firstName()).isEqualTo(expectedResponse.getData().firstName());
        assertThat(actualResponse.getData().lastName()).isEqualTo(expectedResponse.getData().lastName());
        assertThat(actualResponse.getData().email()).isEqualTo(expectedResponse.getData().email());
    }
}