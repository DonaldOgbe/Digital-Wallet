package com.deodev.userService.util;

import com.deodev.userService.exception.TokenValidationException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private JwtSecretUtil secretUtil;

    String subject;
    Map<String, Object> extraClaims;
    List<String> authorities;
    UUID userId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        secretUtil = new JwtSecretUtil();
        secretUtil.setExpiration(3600000L);
        secretUtil.setSecret("hiXdM3nC0mKW68KUVkZFv6O7t04LNRDN639yY5E0KrFFnfyPE57nsJlwAWqOZQ+e");
        jwtUtil = new JwtUtil(secretUtil);
        subject = "subject";
        extraClaims = new HashMap<>();
        extraClaims.put("subjectId", "1234");
        authorities = List.of("USER", "ADMIN");
        extraClaims.put("authorities", authorities);
        extraClaims.put("userId", userId);
    }

    @Test
    void testIfExpiredTokenThrowsError() {
        // given
        String token = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 10L))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        // when + then
        assertThrows(TokenValidationException.class, () -> {
            jwtUtil.isValidToken(token);
        });
    }

    @Test
    void testIfMethodGetsAuthorities() {
        // given
        String token = jwtUtil.generateToken(extraClaims, subject);

        // when
        List<String> result = jwtUtil.getAuthoritiesFromToken(token);

        // then
        assertThat(result).contains("USER", "ADMIN");
    }

    @Test
    void getClaimFromClaims() {
        // given
        String token = jwtUtil.generateToken(extraClaims, subject);

        // when
        UUID id = UUID.fromString(jwtUtil.getClaimFromToken(token,
                claims -> (String) claims.get("userId")));

        // then
        assertThat(id).isEqualTo(userId);
    }

    @Test
    void testIfValidAuthenticationIsGenerated() {
        // given
        String token = jwtUtil.generateToken(extraClaims ,subject);

        // when
        Authentication authentication = jwtUtil.getAuthenticationFromToken(token);
        List<String> tokenAuthorities = authentication.getAuthorities().stream()
                .map(Object::toString)
                .toList();

        // then
        assertThat(subject).isEqualTo(authentication.getPrincipal());
        assertThat(tokenAuthorities).contains("USER", "ADMIN");
    }

    // utility methods
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretUtil.getSecret().getBytes(StandardCharsets.UTF_8));
    }

}