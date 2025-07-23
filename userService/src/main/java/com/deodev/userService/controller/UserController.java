package com.deodev.userService.controller;

import com.deodev.userService.dto.response.ApiResponse;
import com.deodev.userService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<?> searchForUserFromUsername(@RequestParam String username) {

        ApiResponse<?> response = userService.findUserDetails(username);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
