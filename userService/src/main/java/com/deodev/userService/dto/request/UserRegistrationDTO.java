package com.deodev.userService.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDTO {

    @NotBlank(message = "Username cannot be Blank")
    private String username;

    @NotBlank(message = "Username cannot be Blank")
    @Email(message = "Invalid Email Format")
    private String email;

    @NotBlank(message = "password cannot be Blank")
    private String password;
}
