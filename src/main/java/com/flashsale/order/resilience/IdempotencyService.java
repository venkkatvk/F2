package com.flashsale.order.resilience;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:order:";
    private static final Duration KEY_TTL = Duration.ofMinutes(5);

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean acquireKey(String idempotencyKey) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "PROCESSING", KEY_TTL);
        return Boolean.TRUE.equals(success);
    }

    public String getCachedResponse(String idempotencyKey) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        String value = redisTemplate.opsForValue().get(redisKey);
        if (value != null && !"PROCESSING".equals(value)) {
            return value;
        }
        return null;
    }

    public void markCompleted(String idempotencyKey, String responsePayload) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(redisKey, responsePayload, KEY_TTL);
    }
}
