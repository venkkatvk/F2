package com.flashsale.order.service;

import com.flashsale.inventory.lock.InventoryLockManager;
import com.flashsale.order.event.OrderEventProducer;
import com.flashsale.order.event.OrderEventProducer.OrderCreatedEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Subsystem Name: Flash Sale Order Orchestration Service
 * Bounded Context: Flash Sale Domain - Checkout Business Logic
 * Responsibility: Coordinates distributed locks, stock validation, and asynchronous event dispatching.
 */
@Service
public class FlashSaleOrderService {

    private final InventoryLockManager lockManager;
    private final OrderEventProducer eventProducer;

    public FlashSaleOrderService(InventoryLockManager lockManager, OrderEventProducer eventProducer) {
        this.lockManager = lockManager;
        this.eventProducer = eventProducer;
    }

    public String processOrder(String userId, String productId, int quantity, double price) {
        String lockKey = "lock:product:" + productId;

        // Safely execute reservation within the Redis distributed lock boundary
        return lockManager.executeWithLock(lockKey, 2000, 5000, () -> {
            String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8);
            double totalAmount = quantity * price;

            // Construct immutable order event payload
            OrderCreatedEvent event = new OrderCreatedEvent(
                    orderId,
                    userId,
                    productId,
                    quantity,
                    totalAmount,
                    Instant.now()
            );

            // Publish event asynchronously to Apache Kafka
            eventProducer.publishOrderEvent(event);

            return orderId;
        });
    }
}