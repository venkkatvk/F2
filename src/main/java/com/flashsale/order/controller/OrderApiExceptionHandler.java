package com.flashsale.order.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final IdempotencyService idempotencyService;
    private final OrderFulfillmentService fulfillmentService;

    // The Listener waiting at the gates of the Kafka topic
    @KafkaListener(topics = "order-events", groupId = "flash-sale-group")
    @Transactional // Bound to the local database transaction!
    public void consumeOrderEvent(OrderCreatedEvent event) {
        
        String uniqueEventId = event.getEventId(); 

        log.info("Incoming pigeon! Received Event ID: {}", uniqueEventId);

        // 1. Check the Sacred Ledger (The Idempotency Gate)
        if (idempotencyService.hasBeenProcessed(uniqueEventId)) {
            // We have seen this before. Burn the duplicate decree!
            log.warn("Duplicate detected for Event ID: {}. Ignoring safely.", uniqueEventId);
            return; 
        }

        // 2. Process the actual business logic safely
        fulfillmentService.prepareShipment(event);

        // 3. Mark the event as processed in the exact same transaction
        idempotencyService.markAsProcessed(uniqueEventId);
        
        log.info("Successfully processed and recorded Event ID: {}", uniqueEventId);
    }
}
