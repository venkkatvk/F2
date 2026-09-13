package com.flashsale.order.resilience;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class OrderProtectionService {

    @RateLimiter(name = "flashSaleRateLimiter", fallbackMethod = "rateLimiterFallback")
    @CircuitBreaker(name = "redisCircuitBreaker", fallbackMethod = "circuitBreakerFallback")
    public String executeProtectedCheckout(String userId, String productId, Supplier<String> checkoutAction) {
        return checkoutAction.get();
    }

    public String rateLimiterFallback(String userId, String productId, Supplier<String> checkoutAction, Exception ex) {
        throw new RateLimitExceededException("System is experiencing extreme rush. Please wait a few seconds.");
    }

    public String circuitBreakerFallback(String userId, String productId, Supplier<String> checkoutAction, Throwable t) {
        throw new ServiceDegradedException("Checkout queue temporarily paused for system safety. Please try again shortly.");
    }
}
