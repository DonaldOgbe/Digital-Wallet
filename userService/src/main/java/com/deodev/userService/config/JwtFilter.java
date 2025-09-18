package com.deodev.userService.config;

import com.deodev.userService.dto.response.ApiResponse;
import com.deodev.userService.dto.response.ErrorResponse;
import com.deodev.userService.enums.ErrorCode;
import com.deodev.userService.exception.TokenValidationException;
import com.deodev.userService.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.LocalDateTime;
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {

        String jwt = extractToken(request);

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            validateAndAuthenticate(jwt, request);
            filterChain.doFilter(request, response);
        } catch (TokenValidationException e) {
            handleTokenError(request, response, e);
            return;
        } catch (Exception e) {
            handleUnexpectedError(request, response, e);
            return;
        }
    }

    String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    void validateAndAuthenticate(String jwt, HttpServletRequest request) {
        if (!jwtUtil.isValidToken(jwt)) {
            throw new TokenValidationException("Invalid token");
        }
        verifyAndSetAuthentication(jwt);
        setUserIdAttribute(jwt, request);
    }

    void verifyAndSetAuthentication(String jwt) {
        String username = jwtUtil.getUsernameFromToken(jwt);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            SecurityContextHolder.getContext().setAuthentication(jwtUtil.getAuthenticationFromToken(jwt));
        }
    }

    void setUserIdAttribute(String jwt, HttpServletRequest request) {
        Object userId = jwtUtil.getClaimFromToken(jwt, claims -> (String) claims.get("userId"));
        if (userId != null) {
            request.setAttribute("userId", String.valueOf(userId));
        }
    }

    void handleTokenError(HttpServletRequest request, HttpServletResponse response, TokenValidationException e) throws IOException {
        SecurityContextHolder.clearContext();
        log.warn("Token validation failed for [{} {}]: {}", request.getMethod(), request.getRequestURI(), e.getMessage());

        writeErrorResponse(response, HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN,
                "Invalid or expired token", request.getRequestURI());
    }

    void handleUnexpectedError(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException {
        SecurityContextHolder.clearContext();
        log.error("Unexpected error in auth filter for {} {}", request.getMethod(), request.getRequestURI(), e);

        writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.SYSTEM_ERROR,
                "Internal server error", request.getRequestURI());
    }

    void writeErrorResponse(HttpServletResponse response,
                                    HttpStatus status,
                                    ErrorCode errorCode,
                                    String message,
                                    String path) throws IOException {

        ErrorResponse data = ErrorResponse.builder()
                .message(message)
                .path(path)
                .build();

        ApiResponse<ErrorResponse> apiResponse = ApiResponse.<ErrorResponse>builder()
                .success(false)
                .statusCode(status.value())
                .timestamp(LocalDateTime.now())
                .errorCode(errorCode)
                .data(data)
                .build();

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
        response.getOutputStream().flush();
    }
}
