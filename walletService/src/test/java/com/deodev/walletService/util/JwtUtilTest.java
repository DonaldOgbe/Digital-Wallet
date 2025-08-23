package com.deodev.walletService.util;

import com.deodev.walletService.exception.TokenValidationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

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
    void testMethodGeneratesValidTokenWithExtraClaims() throws Exception {
        // when
        String token = jwtUtil.generateToken(extraClaims, subject);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();


        // then
        assertThat(subject).isEqualTo(claims.getSubject());
        assertThat("1234").isEqualTo(claims.get("subjectId"));
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    void testMethodGeneratesValidTokenWithoutExtraClaims() throws Exception {
        // when
        String token = jwtUtil.generateToken(subject);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();


        // then
        assertThat(subject).isEqualTo(claims.getSubject());
        assertThat(claims.getExpiration()).isAfter(new Date());
    }


    @Test
    void testIfTokenIsValid() {
        // given
        String token = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + secretUtil.getExpiration()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        // when
        boolean result = jwtUtil.isValidToken(token);

        // then
        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({
            "''",
            "'invalid.token'",
            "'unsupported'"
    })
    void testIfTokenIsInvalid(String token) {
        // when
        boolean result = jwtUtil.isValidToken(token);

        // then
        assertFalse(result);
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
    void testIfBadSignatureTokenThrowsError() {
        // given
        String token = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 10L))
                .signWith(Keys.hmacShaKeyFor("2ogXJ1cAkqHF54dnX9+s9y6T/NSciplKv2smwio3CTas4HS05+NJQTiMW+BAzjqM"
                                .getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS512)
                .compact();

        // when + then
        assertThrows(TokenValidationException.class, () -> {
            jwtUtil.isValidToken(token);
        });
    }

    @Test
    void testIfMethodGetsSubject() {
        // given
        String token = jwtUtil.generateToken(subject);

        // when
        String result = jwtUtil.getUsernameFromToken(token);

        // then
        assertThat(subject).isEqualTo(result);
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