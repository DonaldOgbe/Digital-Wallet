package com.deodev.userService.util;

import com.deodev.userService.exception.TokenValidationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Component
public class JwtUtil {

    private final JwtSecretUtil secretUtil;

    public String generateToken(Map<String, Object> extraClaims, String subject) {
        return createToken(extraClaims, subject);
    }

    public String generateToken(String subject) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("authorities", List.of("ROLE_USER"));
        return createToken(extraClaims, subject);
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

    public UsernamePasswordAuthenticationToken getAuthenticationFromToken(String token) {
        try {
            validateToken(token);

            final String username = getUsernameFromToken(token);
            final List<String> authorities = getAuthoritiesFromToken(token);

            List<GrantedAuthority> grantedAuthorities = authorities.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toUnmodifiableList());

            return new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
        } catch (Exception e) {
            throw new TokenValidationException(e.getMessage());
        }
    }

    public boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public  <T> T getClaimFromToken(String token, Function<Claims, T> claimsTFunction) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsTFunction.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new TokenValidationException("Invalid JWT Token");
        }

    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretUtil.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
