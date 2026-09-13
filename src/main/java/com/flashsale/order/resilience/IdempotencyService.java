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

    /**
     * Atomically locks the idempotency key using Redis SETNX (Set if Not Exists).
     *
     * @param idempotencyKey Unique UUID passed in HTTP Header (X-Idempotency-Key)
     * @return true if key was successfully reserved (first-time request), false if duplicate request detected
     */
    public boolean acquireKey(String idempotencyKey) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;

        // Redis SETNX execution: Atomically sets key in memory only if it does not exist
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "PROCESSING", KEY_TTL);

        return Boolean.TRUE.equals(success);
    }

    /**
     * Caches the finalized execution result so duplicate calls return identical HTTP responses.
     */
    public void markCompleted(String idempotencyKey, String responsePayload) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(redisKey, responsePayload, KEY_TTL);
    }
}