package com.flashsale.order.service;

import com.flashsale.inventory.lock.InventoryLockManager;
import org.springframework.stereotype.Service;

@Service
public class FlashSaleOrderService {

    private final InventoryLockManager lockManager;
    private final OrderWriteService orderWriteService;

    public FlashSaleOrderService(InventoryLockManager lockManager, OrderWriteService orderWriteService) {
        this.lockManager = lockManager;
        this.orderWriteService = orderWriteService;
    }

    public String processOrder(String userId, String productId, int quantity, double price) {
        String lockKey = "lock:product:" + productId;

        return lockManager.executeWithLock(lockKey, 2000, 5000, () ->
                orderWriteService.createOrderAndStageOutbox(userId, productId, quantity, price)
        );
    }
}
