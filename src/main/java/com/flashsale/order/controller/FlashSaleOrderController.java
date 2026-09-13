package com.flashsale.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class FlashSaleOrderController {

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok("Order received for user: " + request.getUserId());
    }

    public static class OrderRequest {
        private String userId;
        private String productId;
        private int quantity;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}