package com.flashsale.order.controller;

import com.flashsale.order.service.FlashSaleOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class FlashSaleOrderController {

    private final FlashSaleOrderService orderService;

    public FlashSaleOrderController(FlashSaleOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        try {
            // Pass all 4 required arguments (defaulting to 99.99 if price is unprovided)
            double priceToCharge = request.getPrice() > 0 ? request.getPrice() : 99.99;

            orderService.processOrder(
                    request.getUserId(),
                    request.getProductId(),
                    request.getQuantity(),
                    priceToCharge
            );
            return ResponseEntity.ok("Order processing initiated for user: " + request.getUserId());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Order execution failed: " + e.getMessage());
        }
    }

    public static class OrderRequest {
        private String userId;
        private String productId;
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