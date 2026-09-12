package com.flashsale.inventory.lock;

/**
 * Subsystem Name: Inventory Lock Subsystem
 * Bounded Context: Flash Sale Domain - Concurrency Exception Boundary
 * Responsibility: Encapsulates distributed lock acquisition failures, contention timeouts, and thread interruptions.
 */
public class InventoryLockException extends RuntimeException {

    private final String lockKey;

    public InventoryLockException(String message) {
        super(message);
        this.lockKey = "UNKNOWN";
    }

    public InventoryLockException(String message, String lockKey) {
        super(message);
        this.lockKey = lockKey;
    }

    public InventoryLockException(String message, Throwable cause) {
        super(message, cause);
        this.lockKey = "UNKNOWN";
    }

    public InventoryLockException(String message, String lockKey, Throwable cause) {
        super(message, cause);
        this.lockKey = lockKey;
    }

    public String getLockKey() {
        return lockKey;
    }
}