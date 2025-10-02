package com.deodev.userService.service;

import com.deodev.userService.dto.request.UpdatePasswordRequest;
import com.deodev.userService.dto.request.UserRegistrationRequest;
import com.deodev.userService.dto.response.ApiResponse;
import com.deodev.userService.dto.response.GetUserDetailsResponse;
import com.deodev.userService.exception.InvalidPasswordException;
import com.deodev.userService.exception.ResourceNotFoundException;
import com.deodev.userService.exception.UserAlreadyExistsException;
import com.deodev.userService.model.User;
import com.deodev.userService.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ObjectMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final RedisCacheService redisCacheService;

    public GetUserDetailsResponse findUserDetails(UUID userId) {

        User user = userRepository.findById(userId).orElseThrow();

        return GetUserDetailsResponse.builder()
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .build();
    }

    public User createNewUser(UserRegistrationRequest request) {
        User user = mapper.convertValue(request, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(roleService.getDefaultRole()));
        return user;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void validateUser(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email Already Exists");
        }
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));
    }

    @Transactional
    public ApiResponse<Void> updatePassword(String userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        verifyPassword(request.oldPassword(), user.getPassword());
        matchPassword(request.newPassword(), request.confirmPassword());
        User savedUser = setPassword(user, request.newPassword());
        resetRedisCache(savedUser);

        return ApiResponse.success(HttpStatus.OK.value(), null);
    }

    void verifyPassword(String oldPassword, String hashedPassword) {
        if (!passwordEncoder.matches(oldPassword, hashedPassword)) {
            throw new InvalidPasswordException("Old password is incorrect");
        }
    }

    void matchPassword(String password1, String password2) {
        if (!password1.equals(password2)) {
            throw new InvalidPasswordException("New passwords do not match");
        }
    }

    User setPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordUpdatedAt(Instant.now());
        return saveUser(user);
    }

    void resetRedisCache(User user) {
        String userId = user.getId().toString();
        Instant instant = user.getPasswordUpdatedAt();

        redisCacheService.deleteRefreshToken(userId);
        redisCacheService.deletePasswordUpdatedAt(userId);
        redisCacheService.cachePasswordUpdatedAt(userId, instant);
    }

    @Transactional
    public void markUserVerified(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        user.setVerified(true);
        userRepository.save(user);
    }
}
