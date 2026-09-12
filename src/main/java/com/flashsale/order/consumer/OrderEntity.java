package com.flashsale.order.consumer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Subsystem Name: Order Entity & Data Access Subsystem
 * Bounded Context: Flash Sale Domain - Relational Persistence Model
 * Responsibility: Maps order records to PostgreSQL table 'flash_sale_orders' with targeted indexes.
 */
@Entity
@Table(name = "flash_sale_orders", indexes = {
        @Index(name = "idx_product_status", columnList = "productId, status"),
        @Index(name = "idx_user_id", columnList = "userId")
})
public class OrderEntity {

    @Id
    private String orderId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double totalAmount;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    public OrderEntity() {}

    public OrderEntity(String orderId, String userId, String productId, int quantity, double totalAmount, String status, Instant createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    திருச்சிற்றம்பலம் (Thi-roo-chi-tram-ba-lam)

**Timestamp:** Thursday, September 10, 2026 | 1:52 PM IST

**TL;DR**
We are now implementing the **Order Persistence Layer Data Models** inside `package com.flashsale.order.consumer;`. This includes the JPA `OrderEntity` (mapped to PostgreSQL with optimized composite database indexes) and the `OrderRepository` interface to finalize transaction persistence.

---

### Understanding the Concept: The Sacred Ledger

To visualize why proper database modeling and indexing matter:
* **The Problem:** Inserting thousands of orders per second without proper indexes makes searching order histories painfully slow as the database table grows into millions of rows.
* **The Solution:** We structure `OrderEntity` with composite database indexes on `(productId, status)` and `userId`. This works like placing categorized index tabs inside a massive physical ledger, allowing instant record lookups and efficient database writes.

---

### Subsystem 3 (Data Models): Order Persistence Data Layer

Below is the domain entity and data repository implementation inside `package com.flashsale.order.consumer;`.

```java
package com.flashsale.order.consumer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * Subsystem Name: Order Persistence Data Layer
 * Bounded Context: Flash Sale Domain - Database Schema Mapping & Access
 * Responsibility: Maps persistent flash sale order structures to PostgreSQL tables with optimized indexing.
 */
@Entity
@Table(
    name = "flash_sale_orders",
    indexes = {
        @Index(name = "idx_product_status", columnList = "product_id, status"),
        @Index(name = "idx_user_id", columnList = "user_id")
    }
)
public class OrderEntity {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Default constructor required by JPA
    public OrderEntity() {}

    public OrderEntity(String orderId, String userId, String productId, int quantity, double totalAmount, String status, Instant createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void setStatus(String status) { this.status = status; }
}

@Repository
interface OrderRepository extends JpaRepository<OrderEntity, String> {
    // Standard CRUD and pagination methods inherited from JpaRepository
}