package com.flashsale.order.controller;

import com.flashsale.order.outbox.TransactionalOutboxService;
import com.flashsale.order.resilience.IdempotencyService;
import com.flashsale.order.resilience.OrderProtectionService;
import com.flashsale.telemetry.TelemetrySseController;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@RestController("unifiedFlashSaleOrderController")
@RequestMapping("/api/orders")
public class FlashSaleOrderController {

    private final IdempotencyService idempotencyService;
    private final OrderProtectionService protectionService;
    private final TransactionalOutboxService outboxService;
    private final TelemetrySseController telemetrySseController;
    private final RedissonClient redissonClient;

    public FlashSaleOrderController(IdempotencyService idempotencyService,
                                    OrderProtectionService protectionService,
                                    TransactionalOutboxService outboxService,
                                    TelemetrySseController telemetrySseController,
                                    RedissonClient redissonClient) {
        this.idempotencyService = idempotencyService;
        this.protectionService = protectionService;
        this.outboxService = outboxService;
        this.telemetrySseController = telemetrySseController;
        this.redissonClient = redissonClient;
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createFlashSaleOrder(
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody OrderRequest request) {

        // 1. Generate fallback Idempotency Key if header is missing
        String key = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey
                : UUID.randomUUID().toString();

        // 2. Execute protected flow through Resilience4j Rate Limiter & Circuit Breaker
        String result = protectionService.executeProtectedCheckout(
                request.userId(),
                request.productId(),
                () -> processOrderWithLock(key, request)
        );

        // 3. Map execution result to HTTP Status Codes
        if (result.startsWith("TOO_MANY_REQUESTS")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error", result));
        } else if (result.startsWith("SERVICE_DEGRADED")) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", result));
        } else if (result.equals("DUPLICATE_REQUEST")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Duplicate request detected and ignored."));
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "orderId", "ORD-" + key.substring(0, 8),
                "status", "ACCEPTED",
                "message", "Order successfully queued for processing."
        ));
    }

    private void processOrderWithLock(String idempotencyKey, OrderRequest request) {
        // Step A: Redis Idempotency Check (SETNX)
        boolean isNewRequest = idempotencyService.acquireKey(idempotencyKey);
        if (!isNewRequest) {
            throw new IllegalStateException("DUPLICATE_REQUEST");
        }

        // Step B: Redisson Distributed Lock Acquisition
        String lockKey = "lock:product:" + request.productId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(2, 5, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("COULD_NOT_ACQUIRE_LOCK");
            }

            // Step C: Save Order and Stage Outbox Event inside Postgres Transaction
            String orderId = "ORD-" + idempotencyKey.substring(0, 8);
            String jsonPayload = String.format("{\"orderId\":\"%s\",\"userId\":\"%s\",\"productId\":\"%s\",\"price\":%.2f}",
                    orderId, request.userId(), request.productId(), request.price());

            outboxService.stageOutboxEvent(orderId, "ORDER_CREATED", jsonPayload);
            idempotencyService.markCompleted(idempotencyKey, "COMPLETED");

            // Step D: Push Real-Time Telemetry to Dashboard via SSE
            telemetrySseController.broadcast("order-queued", Map.of(
                    "orderId", orderId,
                    "userId", request.userId(),
                    "productId", request.productId(),
                    "timestamp", System.currentTimeMillis()
            ));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while waiting for lock", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public record OrderRequest(String userId, String productId, Integer quantity, Double price) {}
}