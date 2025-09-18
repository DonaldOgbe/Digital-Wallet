package com.deodev.walletService.config;

import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthenticationFilterTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    private final HttpHeaders headers = new HttpHeaders();

    @Autowired
    private ObjectMapper mapper;

    @Test
    void testTokenValidationErrorResponseIsSent() throws Exception {
        headers.set("Authorization", "Bearer dkdkieuksskkskjffuujekddj");

        HttpEntity<Object> requestHttpEntity = new HttpEntity<>(headers);

        // when
        ResponseEntity<ApiResponse<ErrorResponse>> response = testRestTemplate.exchange(
                "/api/v1/wallets",
                HttpMethod.POST,
                requestHttpEntity,
                new ParameterizedTypeReference<ApiResponse<ErrorResponse>>() {}
        );

        ApiResponse<ErrorResponse> body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(body.isSuccess()).isFalse();
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());

        ErrorResponse error = body.getData();
        assertThat(error).isNotNull();
        assertThat(error.message()).isNotBlank();
    }

}