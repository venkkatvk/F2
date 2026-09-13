package com.flashsale.order.resilience;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.stereotype.Service;

@Service
public class OrderProtectionService {

    /**
     * Wraps core flash sale execution with Rate Limiting and Circuit Breaker capabilities.
     * If traffic exceeds allowed TPS or Redis fails repeatedly, fallback methods handle requests cleanly.
     */
    @RateLimiter(name = "flashSaleRateLimiter", fallbackMethod = "rateLimiterFallback")
    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "circuitBreakerFallback")
    public String executeProtectedCheckout(String userId, String productId, Runnable checkoutAction) {
        // Execute core checkout logic (Redis lock acquisition + outbox staging)
        checkoutAction.run();
        return "SUCCESS";
    }

    /**
     * Fallback triggered when client exceeds allowed HTTP request throughput limits (Rate Limiter breach).
     */
    public String rateLimiterFallback(String userId, String productId, Runnable checkoutAction, Exception ex) {
        return "TOO_MANY_REQUESTS: System is experiencing extreme rush. Please wait a few seconds.";
    }

    /**
     * Fallback triggered when downstream infrastructure (like Redis) is down or degraded (Circuit Breaker OPEN).
     */
    public String circuitBreakerFallback(String userId, String productId, Runnable checkoutAction, Throwable t) {
        return "SERVICE_DEGRADED: Checkout queue temporarily paused for system safety. Please try again shortly.";
    }
}