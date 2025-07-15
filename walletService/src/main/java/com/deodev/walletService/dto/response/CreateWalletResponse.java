package com.deodev.walletService.dto.response;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateWalletResponse {
    private UUID walletId;
    private UUID userId;
}
