package com.deodev.userService.controller;

import com.deodev.userService.model.User;
import com.deodev.userService.repository.UserRepository;
import com.deodev.userService.service.RoleService;
import com.deodev.userService.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private User user;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@email.com")
                .password("$2a$10$hashedpw")
                .roles(Set.of(roleService.getDefaultRole()))
                .build();

        userRepository.save(user);
    }

    @Test
    void getUser_ShouldReturnUserDetails_WhenAuthorized() throws Exception {
        // given
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", List.of("ROLE_USER"));

        String jwt = jwtUtil.generateToken(claims, user.getEmail());

        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}", user.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(user.getEmail()))
                .andExpect(jsonPath("$.data.firstname").value(user.getFirstname()))
                .andExpect(jsonPath("$.data.lastname").value(user.getLastname()));
    }

}