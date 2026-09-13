package com.flashsale.order.outbox;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TransactionalOutboxService {

    private final OutboxEventRepository outboxRepository;

    public TransactionalOutboxService(OutboxEventRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

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
