package com.deodev.userService.config;

import com.deodev.userService.dto.response.ApiResponse;
import com.deodev.userService.dto.response.ErrorResponse;
import com.deodev.userService.enums.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationFilterTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void getUser_withInvalidToken_shouldReturnTokenFailureResponse() {
        // given
        UUID userId = UUID.randomUUID();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer invalid-token");

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // when
        ResponseEntity<ApiResponse<ErrorResponse>> response = testRestTemplate.exchange(
                "/api/v1/users/" + userId,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<ApiResponse<ErrorResponse>>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ApiResponse<ErrorResponse> body = response.getBody();
        assertThat(body.isSuccess()).isFalse();
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(body.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);

        ErrorResponse data = body.getData();
        assertThat(data.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(data.errorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
        assertThat(data.message()).containsIgnoringCase("invalid");
        assertThat(data.path()).isEqualTo("/api/v1/users/" + userId);
    }
}