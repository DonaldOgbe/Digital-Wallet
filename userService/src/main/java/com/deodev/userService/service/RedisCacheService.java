package com.deodev.userService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final StringRedisTemplate stringRedisTemplate;

    public void cachePasswordUpdatedAt(String userId, Instant instant) {
        stringRedisTemplate.opsForValue().set("passwordUpdatedAt:"+userId, instant.toString());
    }

    public void cacheRefreshToken(String userId, String token, int days) {
        stringRedisTemplate.opsForValue().set("refreshToken:"+ userId, token, days, TimeUnit.DAYS);
    }
}
