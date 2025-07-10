package com.deodev.userService.dto.response;

import lombok.*;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetUserDetailsResponse {

    private UUID id;
    private String username;
    private String email;
}
