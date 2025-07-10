package com.deodev.userService.service;

import com.deodev.userService.dto.response.ApiResponse;
import com.deodev.userService.dto.response.GetUserDetailsResponse;
import com.deodev.userService.model.User;
import com.deodev.userService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public ApiResponse<?> findUserDetails(String username) {

        User user = userRepository.findByUsername(username).orElseThrow();

        GetUserDetailsResponse response = GetUserDetailsResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        return ApiResponse.success(
                "User: "+ user.getUsername() +" found successfully",
                response);
    }
}
