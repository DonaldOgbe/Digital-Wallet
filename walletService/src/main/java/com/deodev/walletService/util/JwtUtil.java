package com.deodev.walletService.util;


import com.deodev.walletService.exception.TokenValidationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtSecretUtil secretUtil;
    private String cacheToken;

    public String generateToken(Map<String, Object> extraClaims, String subject) {
        return createToken(extraClaims, subject);
    }

    public String generateServiceToken(Map<String, Object> extraClaims) {
        if (cacheToken != null && isValidToken(cacheToken)) {
            return cacheToken;
        }

        cacheToken = generateToken(extraClaims, "wallet-service");
        return cacheToken;
    }

    private String createToken(Map<String, Object> extraClaims, String subject) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + secretUtil.getExpiration()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public List<String> getAuthoritiesFromToken(String token) {
        return getClaimFromToken(token, claims -> (List<String>) claims.get("authorities"));
    }

    public Authentication getAuthenticationFromToken(String token) {
        try {
            final String username = getUsernameFromToken(token);
            final List<String> authorities = getAuthoritiesFromToken(token);

            List<GrantedAuthority> grantedAuthorities = authorities.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toUnmodifiableList());

            return new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
        } catch (SignatureException e) {
            throw new TokenValidationException("Invalid Token");
        }
    }


    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) {
            throw new TokenValidationException("Invalid Token");
        }
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> function) {
        final Claims claims = getAllClaimsFromToken(token);
        return function.apply(claims);
    }

    public boolean isValidToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (SignatureException e) {
            return false;
        }
    }


    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretUtil.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public void clearCachedToken() {
        cacheToken = null;
    }


}
