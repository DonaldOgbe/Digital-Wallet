package com.deodev.walletService.walletService.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRequest {

    @NotNull(message = "user ID cannot be null")
    private UUID userId;
}
