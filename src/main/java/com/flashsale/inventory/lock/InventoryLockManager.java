package com.flashsale.inventory.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Subsystem Name: Inventory Lock Subsystem
 * Bounded Context: Flash Sale Domain - Concurrency Control
 * Responsibility: Acquires and safely releases distributed locks via Redis to eliminate race conditions.
 */
@Component
public class InventoryLockManager {

    private final RedissonClient redissonClient;

    public InventoryLockManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * Executes a business task safely within a distributed lock context.
     *
     * @param lockKey     Unique key for the item (e.g., "lock:product:101")
     * @param waitTimeMs  Maximum time a thread will wait to acquire the lock
     * @param leaseTimeMs Time after which the lock automatically expires (prevents deadlocks)
     * @param operation   The core business task to execute safely
     * @param <T>         Return type of the business task
     * @return Result of the operation execution
     */
    public <T> T executeWithLock(String lockKey, long waitTimeMs, long leaseTimeMs, Supplier<T> operation) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean isAcquired = false;

        try {
            // Attempt to acquire lock without blocking indefinitely
            isAcquired = lock.tryLock(waitTimeMs, leaseTimeMs, TimeUnit.MILLISECONDS);

            if (!isAcquired) {
                throw new InventoryLockException("Unable to acquire lock for key: " + lockKey + ". High traffic conflict.");
            }

            // Execute the isolated business logic safely
            return operation.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InventoryLockException("Lock acquisition interrupted for key: " + lockKey, e);
        } finally {
            // Guarantee lock release ONLY if current thread holds it
            if (isAcquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}