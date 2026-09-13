package com.flashsale.order.outbox;

import jakarta.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TransactionalOutboxService {

    private final OutboxEventRepository outboxRepository;

    public TransactionalOutboxService(OutboxEventRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    /**
     * Saves the outbox event payload within the existing active database transaction.
     * If the main order database save rolls back, this outbox event rolls back automatically.
     */
    @Transactional
    public void stageOutboxEvent(String orderId, String eventType, String jsonPayload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateId(orderId);
        event.setEventType(eventType);
        event.setPayload(jsonPayload);
        event.setStatus("PENDING");
        event.setCreatedAt(LocalDateTime.now());

        outboxRepository.save(event);
    }
}

// Outbox JPA Entity Schema
@Entity
@Table(name = "transactional_outbox")
class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String aggregateId; // e.g., ORD-615cb442

    @Column(nullable = false)
    private String eventType; // e.g., ORDER_CREATED

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload; // Serialized JSON payload

    @Column(nullable = false)
    private String status; // PENDING, SENT, FAILED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Standard Getters & Setters omitted for brevity
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}