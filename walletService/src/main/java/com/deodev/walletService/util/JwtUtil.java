package com.deodev.walletService.util;

import com.deodev.walletService.exception.TokenValidationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtSecretUtil secretUtil;

    public String generateToken(Map<String, Object> extraClaims, String subject) {
        return createToken(extraClaims, subject);
    }

    public String generateToken(String subject) {
        return createToken(subject);
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

    private String createToken(String subject) {
        return Jwts.builder()
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

    public  <T> T getClaimFromToken(String token, Function<Claims, T> function) {
        final Claims claims = getAllClaimsFromToken(token);
        return function.apply(claims);
    }

    public boolean isValidToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new TokenValidationException("Token Expired", e);
        } catch (SignatureException e) {
            throw new TokenValidationException("Invalid Signature", e);
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT: {}", token);
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT format: {}", token);
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT token is null or empty");
            return false;
        }
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretUtil.getSecret().getBytes(StandardCharsets.UTF_8));
    }

}
