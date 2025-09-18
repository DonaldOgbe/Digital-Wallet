package com.deodev.walletService.config;

import com.deodev.walletService.exception.TokenValidationException;
import com.deodev.walletService.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private JwtFilter jwtFilter;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @CsvSource({
            "{}",
            "token"
    })
    void extractToken_ShouldReturnNull_WhenHeaderMissingOrInvalid(String expected) {
        // given
        when(request.getHeader("Authorization")).thenReturn(expected);

        // when
        String token = jwtFilter.extractToken(request);

        // then
        assertNull(token);;
    }

    @Test
    void extractToken_ShouldReturnToken_WhenBearerHeaderPresent() {
        // given
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");

        // when
        String result = jwtFilter.extractToken(request);

        // then
        assertEquals("valid.jwt.token", result);
    }

    @Test
    void validateAndAuthenticate_ShouldThrow_WhenTokenInvalid() {
        // given
        when(jwtUtil.isValidToken("badToken")).thenReturn(false);

        // when & then
        assertThrows(TokenValidationException.class,
                () -> jwtFilter.validateAndAuthenticate("badToken", request));
    }

    @Test
    void validateAndAuthenticate_ShouldSetAuthenticationAndUserId_WhenTokenValid() {
        // given
        when(jwtUtil.isValidToken("goodToken")).thenReturn(true);
        when(jwtUtil.getUsernameFromToken("goodToken")).thenReturn("johndoe@email.com");

        Authentication mockAuth = mock(Authentication.class);
        when(jwtUtil.getAuthenticationFromToken("goodToken")).thenReturn(mockAuth);

        when(jwtUtil.getClaimFromToken(eq("goodToken"), any())).thenReturn("123");

        // when
        jwtFilter.validateAndAuthenticate("goodToken", request);

        // then
        assertEquals(mockAuth, SecurityContextHolder.getContext().getAuthentication());
        verify(request).setAttribute("userId", "123");
    }
}