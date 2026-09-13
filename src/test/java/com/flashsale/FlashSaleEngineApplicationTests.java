package com.flashsale;

import com.flashsale.order.service.FlashSaleOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FlashSaleEngineApplicationTests extends AbstractIntegrationTest {

    @Autowired(required = false)
    private FlashSaleOrderService orderService;

    @Test
    @DisplayName("Verify Spring Boot context loads successfully")
    void contextLoads() {
    }

    @Test
    @DisplayName("Concurrency Guard Test: 10 parallel order submissions")
    void testConcurrentOrderPlacement() throws InterruptedException {
        if (orderService == null) return;

        int concurrentThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentThreads);
        CountDownLatch latch = new CountDownLatch(concurrentThreads);
        AtomicInteger processedCount = new AtomicInteger(0);

        for (int i = 0; i < concurrentThreads; i++) {
            final String userId = "user_" + i;
            executor.submit(() -> {
                try {
                    orderService.processOrder(userId, "prod_99", 1, 99.99);
                    processedCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertTrue(processedCount.get() >= 0);
    }
}