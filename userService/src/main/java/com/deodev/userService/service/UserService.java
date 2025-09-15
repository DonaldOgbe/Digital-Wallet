package com.deodev.userService.service;

import com.deodev.userService.dto.response.GetUserDetailsResponse;
import com.deodev.userService.model.User;
import com.deodev.userService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public GetUserDetailsResponse findUserDetails(UUID userId) {

        User user = userRepository.findById(userId).orElseThrow();

        return GetUserDetailsResponse.builder()
                .firstName(user.getFirstname())
                .lastName(user.getLastname())
                .email(user.getEmail())
                .build();
    }
}
