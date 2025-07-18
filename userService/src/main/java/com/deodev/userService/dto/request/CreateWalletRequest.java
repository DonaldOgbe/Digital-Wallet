package com.deodev.userService.dto.request;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRequest {

    private UUID userId;
}
