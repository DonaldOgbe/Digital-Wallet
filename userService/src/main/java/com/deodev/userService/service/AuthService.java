package com.deodev.userService.service;

import com.deodev.userService.dto.request.UserRegistrationDTO;
import com.deodev.userService.dto.response.ApiResponse;
import com.deodev.userService.exception.UserAlreadyExistsException;
import com.deodev.userService.model.Role;
import com.deodev.userService.model.User;
import com.deodev.userService.repository.RoleRepository;
import com.deodev.userService.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiResponse<Void> register(UserRegistrationDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Email Already Exists");
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new UserAlreadyExistsException("Username Already Exists");
        }

        User user = new ObjectMapper().convertValue(dto, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(Set.of(defaultRole));

        userRepository.save(user);

        return ApiResponse.<Void>builder()
                .message("User Registered Successfully")
                .build();
    }
}
