package com.flashsale.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashsale.inventory.InsufficientStockException;
import com.flashsale.inventory.InventoryRepository;
import com.flashsale.order.consumer.OrderEntity;
import com.flashsale.order.consumer.OrderRepository;
import com.flashsale.order.event.OrderEventProducer.OrderCreatedEvent;
import com.flashsale.order.outbox.TransactionalOutboxService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrderWriteService {

    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final TransactionalOutboxService outboxService;
    private final ObjectMapper objectMapper;

    public OrderWriteService(InventoryRepository inventoryRepository,
                             OrderRepository orderRepository,
                             TransactionalOutboxService outboxService,
                             ObjectMapper objectMapper) {
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
        this.outboxService = outboxService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public String createOrderAndStageOutbox(String userId, String productId, int quantity, double price) {
        int updatedRows = inventoryRepository.decrementStock(productId, quantity);
        if (updatedRows == 0) {
            throw new InsufficientStockException(productId);
        }

        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8);
        double totalAmount = quantity * price;
        Instant now = Instant.now();

        OrderEntity order = new OrderEntity(orderId, userId, productId, quantity, totalAmount, "PENDING", now);
        orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, productId, quantity, totalAmount, now);
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            outboxService.stageOutboxEvent(orderId, "ORDER_CREATED", jsonPayload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize order event for orderId: " + orderId, e);
        }

        return orderId;
    }
}
