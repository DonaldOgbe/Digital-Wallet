package com.deodev.userService.controller;

import com.deodev.userService.dto.request.UpdatePasswordRequest;
import com.deodev.userService.model.User;
import com.deodev.userService.repository.UserRepository;
import com.deodev.userService.service.RedisCacheService;
import com.deodev.userService.service.RoleService;
import com.deodev.userService.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RoleService roleService;
    @MockBean
    private RedisCacheService redisCacheService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@email.com")
                .password(passwordEncoder.encode("12345678"))
                .roles(Set.of(roleService.getDefaultRole()))
                .build();

        userRepository.save(user);
    }

    @Test
    void getUser_ShouldReturnUserDetails_WhenAuthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(user.getEmail()))
                .andExpect(jsonPath("$.data.firstname").value(user.getFirstname()))
                .andExpect(jsonPath("$.data.lastname").value(user.getLastname()));
    }

    @Test
    void updatePassword_shouldReturnSuccessResponse() throws Exception {
        // given
        UpdatePasswordRequest request = new UpdatePasswordRequest(
                "12345678",
                "newPassword123",
                "newPassword123"
        );

        // when + then
        mockMvc.perform(patch("/api/v1/users/me/password")
                        .header("X-User-Id", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.OK.value()));
    }

}