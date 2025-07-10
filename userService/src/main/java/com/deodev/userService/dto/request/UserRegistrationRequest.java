package com.deodev.userService.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationRequest {

    @NotBlank(message = "Username cannot be Blank")
    @Pattern(regexp = "^[a-zA-Z0-9]+$",
    message = "Username can only contain letters, numbers")
    private String username;

    @NotBlank(message = "Username cannot be Blank")
    @Email(message = "Invalid Email Format")
    private String email;

    @NotBlank(message = "Password cannot be Blank")
    private String password;
}
