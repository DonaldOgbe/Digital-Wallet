package com.deodev.userService.controller;

import com.deodev.userService.dto.request.UserRegistrationDTO;
import com.deodev.userService.dto.response.ApiResponse;
import com.deodev.userService.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    private ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDTO dto) {

        ApiResponse<Void> response = authService.register(dto);
        return ResponseEntity.status(response.getStatus())
                .body(response);
    }
}
