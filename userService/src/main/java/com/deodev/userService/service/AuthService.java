package com.deodev.userService.service;

import com.deodev.userService.config.CustomUserDetails;
import com.deodev.userService.dto.request.UserLoginRequest;
import com.deodev.userService.dto.request.UserRegistrationRequest;
import com.deodev.userService.dto.response.UserLoginResponse;
import com.deodev.userService.dto.response.UserRegisteredResponse;
import com.deodev.userService.model.User;
import com.deodev.userService.rabbitmq.publisher.UserEventsPublisher;
import com.deodev.userService.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Autowired
    private final JwtUtil jwtUtil;
    private final UserEventsPublisher userEventsPublisher;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RedisCacheService redisCacheService;

    public UserRegisteredResponse register(UserRegistrationRequest request) {
        userService.validateUser(request.email());

        User savedUser = userService.saveUser(userService.createNewUser(request));

        redisCacheService.cachePasswordUpdatedAt(String.valueOf(savedUser.getId()), savedUser.getPasswordUpdatedAt());

        userEventsPublisher.publishUserRegistered(savedUser);

        CustomUserDetails userDetails = authenticate(request.email(), request.password());

        List<String> authorities = getAuthorities(userDetails);

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", authorities);
        claims.put("userId", userDetails.user().getId());

        String accessToken = jwtUtil.generateToken(claims, userDetails.getUsername());
        String refreshToken = cacheRefreshToken(savedUser.getId());

        return UserRegisteredResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public UserLoginResponse login(UserLoginRequest request) {

        CustomUserDetails userDetails = authenticate(request.email(), request.password());

        List<String> authorities = getAuthorities(userDetails);

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", authorities);
        claims.put("userId", userDetails.user().getId());

        String accessToken = jwtUtil.generateToken(claims, userDetails.getUsername());
        SecurityContextHolder.getContext().setAuthentication(jwtUtil.getAuthenticationFromToken(accessToken));
        String refreshToken = cacheRefreshToken(userDetails.user().getId());


        return UserLoginResponse.builder()
                .user(userDetails.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    CustomUserDetails authenticate(String email, String password) {
        Authentication loginAuthentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        return (CustomUserDetails) loginAuthentication.getPrincipal();
    }

    List<String> getAuthorities(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    String cacheRefreshToken(UUID userId) {
        String token = jwtUtil.generateToken(userId.toString());

        redisCacheService.cacheRefreshToken(String.valueOf(userId), token, 7);

        return token;
    }

}
