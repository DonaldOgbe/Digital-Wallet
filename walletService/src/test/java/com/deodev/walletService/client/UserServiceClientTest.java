package com.deodev.walletService.client;

import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.response.GetUserDetailsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceClientTest {
    @Autowired
    private UserServiceClient userServiceClient;
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
        registry.add("user.service.url", () -> mockWebServer.url("/").toString());
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (mockWebServer != null)
            mockWebServer.close();
    }

    @Test
    void mockUserServiceClientHttpRequestAndGetResponse() throws JsonProcessingException {
        // given
        String userId = UUID.randomUUID().toString();

        ApiResponse<GetUserDetailsResponse> expectedResponse = ApiResponse.<GetUserDetailsResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(GetUserDetailsResponse.builder()
                        .username("username")
                        .firstName("user")
                        .lastName("name")
                        .email("user@gmail.com")
                        .build())
                .build();

        // when
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(expectedResponse)));

        ApiResponse<GetUserDetailsResponse> actualResponse = userServiceClient.getUser(userId, "Bearer fake-token");

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

}