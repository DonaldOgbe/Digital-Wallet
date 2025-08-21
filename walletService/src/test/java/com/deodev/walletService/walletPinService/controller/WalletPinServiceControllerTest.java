package com.deodev.walletService.walletPinService.controller;

import com.deodev.walletService.dto.ErrorResponse;
import com.deodev.walletService.util.JwtUtil;
import com.deodev.walletService.walletPinService.dto.request.SetPinRequest;
import com.deodev.walletService.walletPinService.dto.response.CreateWalletPinResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WalletPinServiceControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    private HttpHeaders headers;
    private String jwt;

    @BeforeEach
    void setUp() {
        headers = new HttpHeaders();
    }

    @Test
    void testWalletPinIsCreatedAnd201IsSent() {

        // then
        UUID walletId = UUID.randomUUID();

        SetPinRequest request = SetPinRequest.builder()
                .newPin("5555")
                .confirmNewPin("5555")
                .build();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("authorities", List.of("ROLE_USER"));

        jwt = jwtUtil.generateToken(extraClaims,"subject");

        headers.set("Authorization", "Bearer ".concat(jwt));

        HttpEntity<SetPinRequest> requestHttpEntity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<CreateWalletPinResponse> response = testRestTemplate.exchange(
                "/api/v1/wallets/{walletId}/pin",
                HttpMethod.POST,
                requestHttpEntity,
                CreateWalletPinResponse.class,
                walletId
        );


        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(walletId).isEqualTo(response.getBody().walletId());
    }

    @Test
    void testThatAccessIsDeniedAndErrorResponseIsSent() {
        // then
        UUID walletId = UUID.randomUUID();

        SetPinRequest request = SetPinRequest.builder()
                .newPin("5555")
                .confirmNewPin("5555")
                .build();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("authorities", List.of("not_user"));

        jwt = jwtUtil.generateToken(extraClaims,"subject");

        headers.set("Authorization", "Bearer ".concat(jwt));

        HttpEntity<SetPinRequest> requestHttpEntity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
                "/api/v1/wallets/{walletId}/pin",
                HttpMethod.POST,
                requestHttpEntity,
                ErrorResponse.class,
                walletId
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().error()).isEqualTo("Authorization Error");
    }









}