package com.deodev.walletService.controller;

import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.request.CreateWalletRequest;
import com.deodev.walletService.dto.response.CreateWalletResponse;
import com.deodev.walletService.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WalletControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    private HttpHeaders headers;
    private String jwt;

    @BeforeEach
    void setUp() {
        headers = new HttpHeaders();
    }

    @Test
    @DisplayName("add new wallet and respond with 201")
    public void walletIsCreatedAndResponseSent() {

        // given
        UUID userId = UUID.randomUUID();
        CreateWalletRequest request = new CreateWalletRequest(userId);

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", List.of("SERVICE"));

        jwtUtil.clearCachedToken();
        headers.set("Authorization", "Bearer ".concat(jwtUtil.generateServiceToken(claims)));

        HttpEntity<CreateWalletRequest> requestHttpEntity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<ApiResponse<CreateWalletResponse>> response = restTemplate.exchange(
                "/api/v1/wallets/create",
                HttpMethod.POST,
                requestHttpEntity,
                new ParameterizedTypeReference<ApiResponse<CreateWalletResponse>>() {
                }
        );

        CreateWalletResponse data = (CreateWalletResponse) Objects.requireNonNull(response.getBody()).getData();

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(userId, data.getUserId());
    }

    @Test
    @DisplayName("Throw validation error when a null null id is used to create a request object")
    public void validationErrorIsThrown() {

        // given
        CreateWalletRequest requestBody = new CreateWalletRequest(null);

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", List.of("SERVICE"));

        headers.set("Authorization", "Bearer ".concat(jwtUtil.generateServiceToken(claims)));

        HttpEntity<CreateWalletRequest> requestHttpEntity = new HttpEntity<>(requestBody, headers);

        // when
        ResponseEntity<ApiResponse<?>> response = restTemplate.exchange(
                "/api/v1/wallets/create",
                HttpMethod.POST,
                requestHttpEntity,
                new ParameterizedTypeReference<ApiResponse<?>>() {
                }
        );

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation Error", response.getBody().getError());
    }

    @Test
    @DisplayName("Throw token validation error when an invalid jwt is used")
    public void tokenValidationErrorIsThrown() {

        // given
        UUID userId = UUID.randomUUID();
        CreateWalletRequest requestBody = new CreateWalletRequest(userId);

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", List.of("SERVICE"));

        headers.set("Authorization", "Bearer ".concat("fake_token"));

        HttpEntity<CreateWalletRequest> requestHttpEntity = new HttpEntity<>(requestBody, headers);

        // when
        ResponseEntity<ApiResponse<?>> response = restTemplate.exchange(
                "/api/v1/wallets/create",
                HttpMethod.POST,
                requestHttpEntity,
                new ParameterizedTypeReference<ApiResponse<?>>() {
                }
        );

        // then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }


    @Test
    @DisplayName("Throw unauthorized error authorities is missing or wrong")
    public void authorizationErrorIsThrown() {

        // given
        UUID userId = UUID.randomUUID();
        CreateWalletRequest requestBody = new CreateWalletRequest(userId);

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", List.of("NOT-SERVICE"));

        headers.set("Authorization", "Bearer ".concat(jwtUtil.generateServiceToken(claims)));

        HttpEntity<CreateWalletRequest> requestHttpEntity = new HttpEntity<>(requestBody, headers);

        // when
        ResponseEntity<ApiResponse<?>> response = restTemplate.exchange(
                "/api/v1/wallets/create",
                HttpMethod.POST,
                requestHttpEntity,
                new ParameterizedTypeReference<ApiResponse<?>>() {
                }
        );

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authorization Error", response.getBody().getError());
    }
}