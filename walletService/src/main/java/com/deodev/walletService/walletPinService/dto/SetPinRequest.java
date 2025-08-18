package com.deodev.walletService.walletPinService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.util.UUID;

@Builder
public record SetPinRequest(
        @NotNull(message = "Confirm PIN cannot be null")
        @NotBlank(message = "Confirm PIN cannot be blank")
        @Pattern(regexp = "\\d{4}", message = "PIN must be exactly 4 digits")
        String newPin,

        @NotNull(message = "Confirm PIN cannot be null")
        @NotBlank(message = "Confirm PIN cannot be blank")
        @Pattern(regexp = "\\d{4}", message = "PIN must be exactly 4 digits")
        String confirmNewPin
) {
}
