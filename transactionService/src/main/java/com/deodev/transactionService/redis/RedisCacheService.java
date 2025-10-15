package com.deodev.transactionService.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisCacheService {
    private final StringRedisTemplate stringRedisTemplate;

    public String getCacheResponse(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void cacheResponse(String key, String response) {
        stringRedisTemplate.opsForValue().set(key, response, Duration.ofHours(24));
    }

    public void cacheChargePayload(String key, String payload) {
        stringRedisTemplate.opsForValue().set(key, payload, Duration.ofMinutes(5));
    }

    public String getCacheChargePayload(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

}
