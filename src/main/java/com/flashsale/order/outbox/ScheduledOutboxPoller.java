package com.flashsale.order.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledOutboxPoller {

    private final OutboxEventRepository outboxRepository;
    private final OrderEventProducer kafkaProducer;

    // The immortal worker sweeps the box every 2000 milliseconds
    @Scheduled(fixedDelay = 2000)
    @Transactional // We need a transaction to update the status safely!
    public void pollAndPublishOutboxEvents() {
        
        // 1. Fetch the unread messages from the wooden box (The Purgatory of events)
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatus("PENDING");

        for (OutboxEvent event : pendingEvents) {
            try {
                // 2. Release the pigeon (Send payload to Kafka)
                kafkaProducer.send(event.getTopic(), event.getPayload());
                
                // 3. Mark as delivered to prevent duplicate dispatches
                event.setStatus("COMPLETED");
                outboxRepository.save(event);
                
                log.info("Successfully published outbox event ID: {}", event.getId());
                
            } catch (Exception e) {
                // If Kafka is down, the pigeon fails, but the message STAYS in PENDING!
                // It will simply be picked up again on the next 2-second sweep.
                // This is the secret to divine resilience!
                log.error("Failed to publish event ID: {}. Will retry.", event.getId(), e);
            }
        }
    }
}
