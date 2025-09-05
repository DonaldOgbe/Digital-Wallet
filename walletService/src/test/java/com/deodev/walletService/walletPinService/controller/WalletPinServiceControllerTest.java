package com.deodev.walletService.walletPinService.controller;

import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.ErrorResponse;
import com.deodev.walletService.util.JwtUtil;
import com.deodev.walletService.walletPinService.dto.request.SetPinRequest;
import com.deodev.walletService.walletPinService.dto.request.UpdatePinRequest;
import com.deodev.walletService.walletPinService.dto.response.CreateWalletPinResponse;

import com.deodev.walletService.walletPinService.dto.response.ValidateWalletPinResponse;
import com.deodev.walletService.walletPinService.model.WalletPin;
import com.deodev.walletService.walletPinService.repository.WalletPinRepository;
import com.deodev.walletService.walletService.service.WalletService;
import jakarta.persistence.EntityManager;
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
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
    private Map<String, Object> extraClaims;
    private String jwt;

    @BeforeEach
    void setUp() {
        headers = new HttpHeaders();
        extraClaims = new HashMap<>();
        extraClaims.put("authorities", List.of("ROLE_USER"));
    }

    @Test
    void testWalletPinIsCreatedAnd201IsSent() {
        // given
        UUID userId = UUID.randomUUID();
        walletService.createWallet(String.valueOf(userId));

        SetPinRequest request = SetPinRequest.builder()
                .newPin("5555")
                .confirmNewPin("5555")
                .build();

        extraClaims.put("userId", userId);

        jwt = jwtUtil.generateToken(extraClaims,"subject");

        headers.set("Authorization", "Bearer ".concat(jwt));

        HttpEntity<SetPinRequest> requestHttpEntity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<ApiResponse<CreateWalletPinResponse>> response = testRestTemplate.exchange(
                "/api/v1/wallets/pin",
                HttpMethod.POST,
                requestHttpEntity,
                new ParameterizedTypeReference<ApiResponse<CreateWalletPinResponse>>() {}
        );

        ApiResponse<CreateWalletPinResponse> body = response.getBody();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(body.getData()).isNotNull();
    }

    @Test
    void testThatPinIsUpdatedAnd200IsSent() {
        // given
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        WalletPin walletPin = WalletPin.builder()
                .walletId(walletId)
                .userId(userId)
                .pin(passwordEncoder.encode("1111"))
                .pinUpdatedAt(LocalDateTime.now())
                .build();

        walletPinRepository.save(walletPin);

        UpdatePinRequest request = UpdatePinRequest.builder()
                .oldPin("1111")
                .newPin("5555")
                .confirmNewPin("5555")
                .build();

        extraClaims.put("userId", userId);

        jwt = jwtUtil.generateToken(extraClaims, "subject");

        headers.set("Authorization", "Bearer ".concat(jwt));

        HttpEntity<UpdatePinRequest> requestHttpEntity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<ApiResponse<CreateWalletPinResponse>> response = testRestTemplate.exchange(
                "/api/v1/wallets/pin",
                HttpMethod.PATCH,
                requestHttpEntity,
                new ParameterizedTypeReference<ApiResponse<CreateWalletPinResponse>>() {}
        );

        ApiResponse<CreateWalletPinResponse> body = response.getBody();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(body.getData()).isNotNull();

        entityManager.clear();
        WalletPin updated = walletPinRepository.findByUserId(userId).orElseThrow();
        assertThat(passwordEncoder.matches("5555", updated.getPin())).isTrue();
    }

    @Test
    void validatePin_ReturnOk_AndSuccess() {
        // given
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        WalletPin walletPin = WalletPin.builder()
                .walletId(walletId)
                .userId(userId)
                .pin(passwordEncoder.encode("1111"))
                .pinUpdatedAt(LocalDateTime.now())
                .build();

        walletPinRepository.save(walletPin);

        extraClaims.put("userId", userId);

        jwt = jwtUtil.generateToken(extraClaims, "subject");

        headers.set("Authorization", "Bearer ".concat(jwt));
        headers.set("Wallet-Pin", "1111");

        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        // when
        ResponseEntity<ApiResponse<ValidateWalletPinResponse>> response = testRestTemplate.exchange(
                "/api/v1/wallets/pin/validate",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<ApiResponse<ValidateWalletPinResponse>>() {}
        );

        ApiResponse<ValidateWalletPinResponse> body =  response.getBody();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(body.getData()).isNotNull();
    }

}