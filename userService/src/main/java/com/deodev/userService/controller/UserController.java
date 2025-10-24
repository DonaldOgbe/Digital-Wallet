package com.deodev.userService.controller;

import com.deodev.userService.dto.request.UpdatePasswordRequest;
import com.deodev.userService.dto.response.ApiResponse;
import com.deodev.userService.dto.response.GetUserDetailsResponse;
import com.deodev.userService.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<?>> getUser(@PathVariable UUID userId) {
        ApiResponse<?> response = userService.findUserDetails(userId);
        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<?>> updatePassword(@Valid @RequestBody UpdatePasswordRequest request,
                                                         @RequestHeader("X-User-Id") String userId) {
        ApiResponse<?> response = userService.updatePassword(userId, request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
