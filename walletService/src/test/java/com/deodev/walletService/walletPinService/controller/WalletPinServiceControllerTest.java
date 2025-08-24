package com.deodev.walletService.walletPinService.controller;

import com.deodev.walletService.dto.ErrorResponse;
import com.deodev.walletService.util.JwtUtil;
import com.deodev.walletService.walletPinService.dto.request.SetPinRequest;
import com.deodev.walletService.walletPinService.dto.request.UpdatePinRequest;
import com.deodev.walletService.walletPinService.dto.response.CreateWalletPinResponse;

import com.deodev.walletService.walletPinService.model.WalletPin;
import com.deodev.walletService.walletPinService.repository.WalletPinRepository;
import com.deodev.walletService.walletService.dto.request.CreateWalletRequest;
import com.deodev.walletService.walletService.service.WalletService;
import jakarta.persistence.EntityManager;
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WalletPinRepository walletPinRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private WalletService walletService;

    private HttpHeaders headers;
    private String jwt;

    @BeforeEach
    void setUp() {
        headers = new HttpHeaders();
    }

    @Test
    void testWalletPinIsCreatedAnd201IsSent() {
        // given
        UUID userId = UUID.randomUUID();
        walletService.createWallet(CreateWalletRequest.builder()
                .userId(userId)
                .build());

        SetPinRequest request = SetPinRequest.builder()
                .newPin("5555")
                .confirmNewPin("5555")
                .build();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("authorities", List.of("ROLE_USER"));
        extraClaims.put("userId", userId);

        jwt = jwtUtil.generateToken(extraClaims,"subject");

        headers.set("Authorization", "Bearer ".concat(jwt));

        HttpEntity<SetPinRequest> requestHttpEntity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<CreateWalletPinResponse> response = testRestTemplate.exchange(
                "/api/v1/wallets/pin",
                HttpMethod.POST,
                requestHttpEntity,
                CreateWalletPinResponse.class
        );

        CreateWalletPinResponse body = response.getBody();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(userId).isEqualTo(body.userId());
    }

    @Test
    @Description("Test that the set pin endpoint is forbidden for unauthorized user")
    void testThatAccessIsDeniedAndErrorResponseIsSentForSetPin() {
        // then
        UUID userId = UUID.randomUUID();

        SetPinRequest request = SetPinRequest.builder()
                .newPin("5555")
                .confirmNewPin("5555")
                .build();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("authorities", List.of("not_user"));
        extraClaims.put("userId", userId);

        jwt = jwtUtil.generateToken(extraClaims,"subject");

        headers.set("Authorization", "Bearer ".concat(jwt));

        HttpEntity<SetPinRequest> requestHttpEntity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
                "/api/v1/wallets/pin",
                HttpMethod.POST,
                requestHttpEntity,
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().error()).isEqualTo("Authorization Error");
    }

    @Test
    void testThatPinIsUpdatedAnd200IsSent() {
        // given
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        WalletPin walletPin = WalletPin.builder()
                .walletId(walletId)
                .userId(userId)
                .pin(passwordEncoder.encode("1111")) // old pin
                .pinUpdatedAt(LocalDateTime.now())
                .build();

        walletPinRepository.save(walletPin);

        UpdatePinRequest request = UpdatePinRequest.builder()
                .oldPin("1111")
                .newPin("5555")
                .confirmNewPin("5555")
                .build();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("authorities", List.of("ROLE_USER"));
        extraClaims.put("userId", userId);

        jwt = jwtUtil.generateToken(extraClaims, "subject");

        headers.set("Authorization", "Bearer ".concat(jwt));

        HttpEntity<UpdatePinRequest> requestHttpEntity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<CreateWalletPinResponse> response = testRestTemplate.exchange(
                "/api/v1/wallets/pin",
                HttpMethod.PATCH,
                requestHttpEntity,
                CreateWalletPinResponse.class
        );

        CreateWalletPinResponse body = response.getBody();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(walletId).isEqualTo(body.walletId());
        assertThat(userId).isEqualTo(body.userId());

        entityManager.clear();
        WalletPin updated = walletPinRepository.findByUserId(userId).orElseThrow();
        assertThat(passwordEncoder.matches("5555", updated.getPin())).isTrue();
    }

    @Test
    @Description("Test that the set pin endpoint is forbidden for unauthorized user")
    void testThatAccessIsDeniedAndErrorResponseIsSentForUpdatePin() {
        // given

        UUID userId = UUID.randomUUID();

        UpdatePinRequest request = UpdatePinRequest.builder()
                .oldPin("1111")
                .newPin("5555")
                .confirmNewPin("5555")
                .build();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("authorities", List.of("NOT_USER"));
        extraClaims.put("userId", userId);

        jwt = jwtUtil.generateToken(extraClaims, "subject");

        headers.set("Authorization", "Bearer ".concat(jwt));

        HttpEntity<UpdatePinRequest> requestHttpEntity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
                "/api/v1/wallets/pin",
                HttpMethod.PATCH,
                requestHttpEntity,
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().error()).isEqualTo("Authorization Error");
    }








}