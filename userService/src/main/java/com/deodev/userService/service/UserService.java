package com.deodev.userService.service;

import com.deodev.userService.dto.request.UserRegistrationRequest;
import com.deodev.userService.dto.response.GetUserDetailsResponse;
import com.deodev.userService.exception.UserAlreadyExistsException;
import com.deodev.userService.model.User;
import com.deodev.userService.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ObjectMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

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
}
