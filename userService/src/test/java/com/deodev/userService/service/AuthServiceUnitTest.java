package com.deodev.userService.service;

import com.deodev.userService.config.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticateUser_ShouldReturnUserDetails_WhenAuthenticationSucceeds() {
        // given
        CustomUserDetails mockUserDetails = mock(CustomUserDetails.class);
        Authentication mockAuthentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any())).thenReturn(mockAuthentication);
        when(mockAuthentication.getPrincipal()).thenReturn(mockUserDetails);

        UserDetails result = authService.authenticate("test@example.com", "password");

        assertEquals(mockUserDetails, result);
    }
}