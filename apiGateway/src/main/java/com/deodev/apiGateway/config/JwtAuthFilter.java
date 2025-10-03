package com.deodev.apiGateway.config;

import com.deodev.apiGateway.service.RedisCacheService;
import com.deodev.apiGateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter {

    private final JwtUtil jwtUtil;
    private final RedisCacheService redisCacheService;

    private static final List<String> WHITELIST = List.of(
            "/api/v1/auth/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (WHITELIST.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isValidToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String userId = jwtUtil.getClaimFromToken(token, claims -> (String) claims.get("userId"));
        Instant iat = jwtUtil.getClaimFromToken(token, claims -> Instant.parse((String) claims.get("iat")));
        Instant redisPasswordUpdatedAt =  Instant.parse(redisCacheService.getPasswordUpdatedAt(userId));

        if (redisPasswordUpdatedAt.isAfter(iat)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(
                exchange.mutate()
                        .request(exchange.getRequest()
                                .mutate()
                                .header("X-User-Id", userId)
                                .build())
                        .build());
    }

}
