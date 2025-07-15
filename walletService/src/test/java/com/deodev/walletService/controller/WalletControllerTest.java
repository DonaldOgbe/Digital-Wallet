package com.deodev.walletService.controller;

import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.request.CreateWalletRequest;
import com.deodev.walletService.dto.response.CreateWalletResponse;
import com.deodev.walletService.util.JwtUtil;
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

//    @BeforeEach
//    void setUp() {
//    }

    @Test
    public void walletIsCreatedAndResponseSent() {

        // given
        UUID userId = UUID.fromString("9419905b-8aa4-4865-bb88-fef48109feca");
        CreateWalletRequest request = new CreateWalletRequest(userId);

        HttpHeaders headers = new HttpHeaders();
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", List.of("SERVICE"));
        headers.set("Authorization", "Bearer ".concat(jwtUtil.generateServiceToken(claims)));

        HttpEntity<CreateWalletRequest> requestHttpEntity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<ApiResponse<CreateWalletResponse>> response = restTemplate.exchange(
                "/api/v1/wallets/create",
                HttpMethod.POST,
                requestHttpEntity,
                new ParameterizedTypeReference<ApiResponse<CreateWalletResponse>>() {}
        );

        CreateWalletResponse data = (CreateWalletResponse) Objects.requireNonNull(response.getBody()).getData();

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(userId, data.getUserId());
    }
}