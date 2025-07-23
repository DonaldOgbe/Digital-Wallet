package com.deodev.userService.service;

import com.deodev.userService.client.WalletServiceClient;
import com.deodev.userService.dto.request.CreateWalletRequest;
import com.deodev.userService.dto.request.UserLoginRequest;
import com.deodev.userService.dto.request.UserRegistrationRequest;
import com.deodev.userService.dto.response.ApiResponse;
import com.deodev.userService.dto.response.CreateWalletResponse;
import com.deodev.userService.dto.response.UserRegisteredResponse;
import com.deodev.userService.exception.UserAlreadyExistsException;
import com.deodev.userService.model.Role;
import com.deodev.userService.model.User;
import com.deodev.userService.model.enums.UserStatus;
import com.deodev.userService.repository.RoleRepository;
import com.deodev.userService.repository.UserRepository;
import com.deodev.userService.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Autowired
    private EntityManager entityManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final WalletServiceClient walletServiceClient;

    public ApiResponse<?> register(UserRegistrationRequest request) {

        validateUser(request.getUsername(), request.getEmail());

        User user = new ObjectMapper().convertValue(request, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(Set.of(defaultRole));

        User savedUser = userRepository.save(user);

        CreateWalletResponse walletResponse = createWallet(
                savedUser.getId(),
                jwtUtil.generateToken(savedUser.getUsername()));

        if (walletResponse.success()) {
            updateUserStatus(savedUser.getId(), UserStatus.ACTIVE);
            entityManager.clear();
            User updatedUser = userRepository.findByUsername(savedUser.getUsername()).orElseThrow();

            return ApiResponse.success("User Registered Successfully",
            UserRegisteredResponse
                    .builder()
                    .userId(updatedUser.getId())
                    .walletId(walletResponse.walletId())
                    .username(updatedUser.getUsername())
                    .email(updatedUser.getEmail())
                    .status(updatedUser.getStatus())
                    .registeredAt(LocalDateTime.now())
                    .build());
        }

        return ApiResponse.<UserRegisteredResponse>builder()
                .message("User Registration Incomplete")
                .note("Wallet service failure")
                .data(UserRegisteredResponse
                        .builder()
                        .userId(savedUser.getId())
                        .walletId(walletResponse.walletId())
                        .username(savedUser.getUsername())
                        .email(savedUser.getEmail())
                        .status(savedUser.getStatus())
                        .registeredAt(LocalDateTime.now())
                        .build())
                .build();
    }

    public ApiResponse<String> login(UserLoginRequest request) {
        try {
            Authentication loginAuthentication =  authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            UserDetails userDetails = (UserDetails) loginAuthentication.getPrincipal();

            List<String> authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            Map<String, Object> claims = new HashMap<>();
            claims.put("authorities", authorities);

            String jwt = jwtUtil.generateToken(claims, userDetails.getUsername());
            SecurityContextHolder.getContext().setAuthentication(jwtUtil.getAuthenticationFromToken(jwt));

            return ApiResponse.success("Login Successful", jwt);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // helper methods

    private CreateWalletResponse createWallet(UUID userId, String jwt) {
        try {
            return walletServiceClient.createWallet(
                    CreateWalletRequest
                            .builder()
                            .userId(userId)
                            .build(),
                    "Bearer ".concat(jwt));
        } catch (FeignException e) {
            return CreateWalletResponse.builder()
                    .success(false)
                    .build();
        }
    }

    private void updateUserStatus(UUID userId, UserStatus status) {
        userRepository.updateUserStatus(userId, status);
    }

    private void validateUser(String username, String email) {
        try {
            if (userRepository.existsByEmail(email)) {
                throw new UserAlreadyExistsException("Email Already Exists");
            }

            if (userRepository.existsByUsername(username)) {
                throw new UserAlreadyExistsException("Username Already Exists");
            }
        } catch (UserAlreadyExistsException e) {
            throw new UserAlreadyExistsException(e.getMessage());
        }
    }
}
