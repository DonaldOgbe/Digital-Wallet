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
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            if (!jwtUtil.isValidToken(jwt)) {
                throw new TokenValidationException("Invalid token");
            }

            String username = jwtUtil.getUsernameFromToken(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                SecurityContextHolder.getContext().setAuthentication(jwtUtil.getAuthenticationFromToken(jwt));
            }

            Object userId = jwtUtil.getClaimFromToken(jwt, claims -> (String) claims.get("userId"));

            if (userId != null) {
                request.setAttribute("userId", String.valueOf(userId));
            }

            filterChain.doFilter(request, response);
        } catch (TokenValidationException e) {
            SecurityContextHolder.clearContext();
            log.warn("Token validation failed for [{} {}]: {}", request.getMethod(), request.getRequestURI(), e.getMessage());

            ErrorResponse data = ErrorResponse.builder()
                    .message("Invalid or expired token")
                    .path(request.getRequestURI())
                    .build();

            ApiResponse<ErrorResponse> apiResponse = ApiResponse.<ErrorResponse>builder()
                    .success(false)
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .timestamp(LocalDateTime.now())
                    .errorCode(ErrorCode.INVALID_TOKEN)
                    .data(data)
                    .build();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), apiResponse);
            response.getOutputStream().flush();
            return;
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.error("Unexpected error in auth filter for {} {}", request.getMethod(), request.getRequestURI(), e);

            ErrorResponse data = ErrorResponse.builder()
                    .message("Internal server error")
                    .path(request.getRequestURI())
                    .build();

            ApiResponse<ErrorResponse> apiResponse = ApiResponse.<ErrorResponse>builder()
                    .success(false)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .timestamp(LocalDateTime.now())
                    .errorCode(ErrorCode.SYSTEM_ERROR)
                    .data(data)
                    .build();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), apiResponse);
            response.getOutputStream().flush();
            return;
        }
    }
}
