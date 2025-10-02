package com.deodev.userService.dto.request;

public record UpdatePasswordRequest(
        String oldPassword,
        String newPassword,
        String confirmPassword) {
}
