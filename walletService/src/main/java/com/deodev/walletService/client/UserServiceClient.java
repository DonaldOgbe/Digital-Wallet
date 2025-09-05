package com.deodev.walletService.client;

import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.response.GetUserDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "user-service",
        url = "${user.service.url}"
)
public interface UserServiceClient {
    @GetMapping("/api/v1/users/{userId}")
    ApiResponse<GetUserDetailsResponse> getUser(@PathVariable String userId,
                                               @RequestHeader("Authorization") String AuthToken);
}
