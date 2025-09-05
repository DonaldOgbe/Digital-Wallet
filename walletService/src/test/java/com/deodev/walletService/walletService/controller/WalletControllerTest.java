package com.deodev.walletService.walletService.controller;

import com.deodev.walletService.dto.ErrorResponse;
import com.deodev.walletService.walletService.dto.response.CreateWalletResponse;
import com.deodev.walletService.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.Assertions.*;


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

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", List.of("ROLE_USER"));
        claims.put("userId", userId);

        jwtUtil.clearCachedToken();
        headers.set("Authorization", "Bearer ".concat(jwtUtil.generateServiceToken(claims)));

        HttpEntity<Object> requestHttpEntity = new HttpEntity<>(headers);

        // when
        ResponseEntity<CreateWalletResponse> response = restTemplate.exchange(
                "/api/v1/wallets",
                HttpMethod.POST,
                requestHttpEntity,
                CreateWalletResponse.class
        );

        CreateWalletResponse body = response.getBody();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(body.userId()).isEqualTo(userId);
    }


    @Test
    @DisplayName("Throw unauthorized error authorities is missing or wrong")
    public void authorizationErrorIsThrown() {

        // given
        UUID userId = UUID.randomUUID();

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", List.of("NOT-ROLE_USER"));
        claims.put("userId", userId);

        headers.set("Authorization", "Bearer ".concat(jwtUtil.generateServiceToken(claims)));

        HttpEntity<Object> requestHttpEntity = new HttpEntity<>(headers);

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/wallets",
                HttpMethod.POST,
                requestHttpEntity,
                ErrorResponse.class
        );

        ErrorResponse body = response.getBody();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(body.errorCode()).isEqualTo("Authorization Error");
    }
}