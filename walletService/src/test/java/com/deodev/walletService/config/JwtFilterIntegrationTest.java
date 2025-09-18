package com.deodev.walletService.config;

import com.deodev.walletService.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class JwtFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private JwtUtil mockJwtUtil;

    @Test
    void request_ShouldReturn401_WhenTokenIsInvalid() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/wallets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer badToken"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.errorCode").value("INVALID_TOKEN"));
    }

    @Test
    void request_ShouldReturn500_WhenUnexpectedExceptionOccurs() throws Exception {
        // given
        when(mockJwtUtil.isValidToken("boomToken")).thenThrow(new RuntimeException("Unexpected"));

        // when & then
        mockMvc.perform(get("/api/v1/users/some-id")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer boomToken"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.errorCode").value("SYSTEM_ERROR"));
    }
}