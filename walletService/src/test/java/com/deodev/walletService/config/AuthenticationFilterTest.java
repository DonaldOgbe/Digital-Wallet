package com.deodev.walletService.config;

import com.deodev.walletService.exception.TokenValidationException;
import com.deodev.walletService.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationFilter.class)
class AuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        when(jwtUtil.getUsernameFromToken(anyString())).thenThrow(TokenValidationException.class);
    }

    @Test
    void testTokenValidationErrorResponseIsSent() throws Exception {
        mockMvc.perform(post("/api/v1/wallets")
                .header("Authorization", "Bearer fake_token"))
                .andExpect(status().isForbidden());
    }

}