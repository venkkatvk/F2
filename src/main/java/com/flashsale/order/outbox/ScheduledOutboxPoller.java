package com.flashsale.order.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class ScheduledOutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(ScheduledOutboxPoller.class);
    private static final String KAFKA_TOPIC = "flash-sale-orders";

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public ScheduledOutboxPoller(OutboxEventRepository outboxRepository,
                                 KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findTop10ByStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxEvent event : pendingEvents) {
            try {
                kafkaTemplate.send(KAFKA_TOPIC, event.getAggregateId(), event.getPayload()).get();
                event.setStatus("SENT");
                outboxRepository.save(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                event.setStatus("FAILED");
                outboxRepository.save(event);
                log.error("Outbox publish interrupted for orderId: {}", event.getAggregateId(), e);
            } catch (ExecutionException e) {
                event.setStatus("FAILED");
                outboxRepository.save(event);
                log.error("Outbox publish failed for orderId: {}", event.getAggregateId(), e.getCause());
            }
        }
    }
}
