package com.deodev.apiGateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final StringRedisTemplate stringRedisTemplate;

    public String getPasswordUpdatedAt(String userId) {
        return stringRedisTemplate.opsForValue().get("passwordUpdatedAt:" + userId);
    }
}
