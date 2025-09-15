package com.deodev.userService.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.*;

@Builder
public record UserLoginRequest(
        @NotBlank(message = "Email cannot be Blank")
        @Email(message = "Invalid Email Format")
        String email,

        @NotBlank(message = "Password cannot be Blank")
        String password
) {
}
