package com.deodev.userService.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Builder

public record UserRegistrationRequest(
        @NotBlank(message = "First name cannot be Blank")
        String firstname,

        @NotBlank(message = "Last name cannot be Blank")
        String lastname,

        @NotBlank(message = "Email cannot be Blank")
        @Email(message = "Invalid Email Format")
        String email,

        @NotBlank(message = "Password cannot be Blank")
        String password
) {
}
