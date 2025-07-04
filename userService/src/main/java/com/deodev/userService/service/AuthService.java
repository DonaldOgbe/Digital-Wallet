package com.deodev.userService.service;

import com.deodev.userService.dto.request.UserRegistrationDTO;
import com.deodev.userService.repository.UserRepository;
import org.springframework.http.ResponseEntity;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<?> register(UserRegistrationDTO dto) {
        return ResponseEntity.ok("Ok");
    }
}
