package com.deodev.userService.controller;

import com.deodev.userService.dto.request.UserLoginRequest;
import com.deodev.userService.dto.request.UserRegistrationRequest;
import com.deodev.userService.enums.UserStatus;
import com.deodev.userService.model.User;
import com.deodev.userService.rabbitmq.publisher.UserEventsPublisher;
import com.deodev.userService.repository.UserRepository;
import com.deodev.userService.service.RedisCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private UserEventsPublisher userEventsPublisher;

    @MockBean
    private RedisCacheService redisCacheService;

    @BeforeEach
    void setup() {
        userRepository.deleteAll(); // clean state
    }

    @Test
    void registerUser_ShouldReturnRegisteredResponse_AndPublishEvent() throws Exception {
        // given
        UserRegistrationRequest requestBody = UserRegistrationRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@email.com")
                .password("12345678")
                .build();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        // when & then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("johndoe@email.com"))
                .andExpect(jsonPath("$.data.userId").isNotEmpty())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());

        verify(userEventsPublisher, times(1)).publishUserRegistered(captor.capture());
        User capturedUser = captor.getValue();
        assertThat(capturedUser.getEmail()).isEqualTo("johndoe@email.com");
    }

    @Test
    void login_ShouldReturnJWT_WhenUserExists() throws Exception {
        // given
        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@email.com")
                .password(passwordEncoder.encode("12345678"))
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        UserLoginRequest requestBody = UserLoginRequest.builder()
                .email("johndoe@email.com")
                .password("12345678")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user").value("johndoe@email.com"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }
}
