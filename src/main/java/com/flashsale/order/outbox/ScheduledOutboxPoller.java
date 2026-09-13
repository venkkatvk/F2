package com.flashsale.order.outbox;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ScheduledOutboxPoller {

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String KAFKA_TOPIC = "flash-sale-orders";

    public ScheduledOutboxPoller(OutboxEventRepository outboxRepository,
                                 KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Executes periodically to poll un-sent events and stream them to Kafka.
     * Uses fixedDelay to prevent overlapping thread executions.
     */
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void processOutboxEvents() {
        // Fetch oldest 10 pending records to maintain low memory overhead under high load
        List<OutboxEvent> pendingEvents = outboxRepository.findTop10ByStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxEvent event : pendingEvents) {
            try {
                // Asynchronously dispatch payload to Kafka broker
                kafkaTemplate.send(KAFKA_TOPIC, event.getAggregateId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                event.setStatus("SENT");
                            } else {
                                event.setStatus("FAILED");
                            }
                            outboxRepository.save(event);
                        });
            } catch (Exception e) {
                event.setStatus("FAILED");
                outboxRepository.save(event);
            }
        }
    }
}