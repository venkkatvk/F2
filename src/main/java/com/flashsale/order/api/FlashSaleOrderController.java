package com.flashsale.order.api;

import com.flashsale.order.service.FlashSaleOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Subsystem Name: Flash Sale REST API Controller
 * Bounded Context: Flash Sale Domain - HTTP API Entry Point
 * Responsibility: Exposes RESTful endpoints for receiving flash sale checkout traffic.
 */
@RestController
@RequestMapping("/api/v1/flash-sale")
public class FlashSaleOrderController {

    private final FlashSaleOrderService orderService;

    public FlashSaleOrderController(FlashSaleOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        String orderId = orderService.processOrder(
                request.userId(),
                request.productId(),
                request.quantity(),
                request.price()
        );

        return ResponseEntity.accepted()
                .body(new OrderResponse(orderId, "ACCEPTED", "Order successfully queued for processing."));
    }

    // Java 21 Records for concise, immutable DTO contracts
    public record OrderRequest(String userId, String productId, int quantity, double price) {}
    public record OrderResponse(String orderId, String status, String message) {}
}