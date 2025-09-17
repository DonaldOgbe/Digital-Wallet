package com.deodev.userService.service;

import com.deodev.userService.dto.request.UserRegistrationRequest;
import com.deodev.userService.exception.UserAlreadyExistsException;
import com.deodev.userService.model.Role;
import com.deodev.userService.model.User;
import com.deodev.userService.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RoleService roleService;
    @Mock private ObjectMapper mapper;
    @InjectMocks private UserService userService;

    @Test
    void createNewUser_ShouldReturnUserWithEncodedPasswordAndDefaultRole() {
        // given
        UserRegistrationRequest request = mock(UserRegistrationRequest.class);
        User mappedUser = new User();

        Role defaultRole = Role.builder().name("ROLE_USER").build();

        when(mapper.convertValue(request, User.class)).thenReturn(mappedUser);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(roleService.getDefaultRole()).thenReturn(defaultRole);

        // when
        User result = userService.createNewUser(request);

        // then
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getRoles().contains(defaultRole));
        assertEquals(mappedUser, result);
    }

    @Test
    void saveUser_ShouldReturnSavedUser_WhenRepositorySucceeds() {
        // given
        User user = new User();
        when(userRepository.save(user)).thenReturn(user);

        // when
        User result = userService.saveUser(user);

        // then
        assertEquals(user, result);
    }

    @Test
    void validateUser_ShouldNotThrow_WhenEmailDoesNotExist() {
        // given
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);

        // when & then
        assertDoesNotThrow(() -> userService.validateUser("new@test.com"));
    }

    @Test
    void validateUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // given
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        // when & then
        assertThrows(UserAlreadyExistsException.class,
                () -> userService.validateUser("existing@test.com"));
    }

}