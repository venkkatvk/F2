package com.flashsale.order.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashsale.order.controller.dto.OrderResponse;
import com.flashsale.order.resilience.IdempotencyService;
import com.flashsale.order.resilience.OrderProtectionService;
import com.flashsale.order.service.FlashSaleOrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class FlashSaleOrderController {

    private final FlashSaleOrderService orderService;
    private final OrderProtectionService orderProtectionService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public FlashSaleOrderController(FlashSaleOrderService orderService,
                                    OrderProtectionService orderProtectionService,
                                    IdempotencyService idempotencyService,
                                    ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.orderProtectionService = orderProtectionService;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody OrderRequest request) throws JsonProcessingException {

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            if (!idempotencyService.acquireKey(idempotencyKey)) {
                String cached = idempotencyService.getCachedResponse(idempotencyKey);
                if (cached != null) {
                    return ResponseEntity.ok(objectMapper.readValue(cached, OrderResponse.class));
                }
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new OrderResponse(null, "DUPLICATE_IN_PROGRESS"));
            }
        }

        double priceToCharge = request.getPrice() > 0 ? request.getPrice() : 99.99;

        String orderId = orderProtectionService.executeProtectedCheckout(
                request.getUserId(),
                request.getProductId(),
                () -> orderService.processOrder(
                        request.getUserId(),
                        request.getProductId(),
                        request.getQuantity(),
                        priceToCharge
                )
        );

        OrderResponse response = new OrderResponse(orderId, "PENDING");

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyService.markCompleted(idempotencyKey, objectMapper.writeValueAsString(response));
        }

        return ResponseEntity.accepted().body(response);
    }

    public static class OrderRequest {
        @NotBlank
        private String userId;
        @NotBlank
        private String productId;
        @Min(1)
        private int quantity;
        private double price;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }
}
