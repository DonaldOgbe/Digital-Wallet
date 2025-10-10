package com.deodev.transactionService.client;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.response.GetUserDetailsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"user.service.url=http://localhost:${wiremock.server.port}"})
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
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(expectedResponse))));

        // when
        ResponseEntity<ApiResponse<?>> actualResponse = userServiceClient.getUser(userId);

        GetUserDetailsResponse data = mapper.convertValue(actualResponse.getBody().getData(), GetUserDetailsResponse.class);

        // then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResponse.getBody().isSuccess()).isTrue();
        assertThat(data.firstName()).isEqualTo(expectedResponse.getData().firstName());
        assertThat(data.lastName()).isEqualTo(expectedResponse.getData().lastName());
        assertThat(data.email()).isEqualTo(expectedResponse.getData().email());
    }

}