package com.flashsale.order.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * Subsystem Name: Order Event Queue Subsystem
 * Bounded Context: Flash Sale Domain - Asynchronous Event Dispatching
 * Responsibility: Publishes immutable order creation events to Apache Kafka topics asynchronously.
 */
@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    private static final String FLASH_SALE_TOPIC = "flash-sale-orders";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes an order event to Kafka using the product ID as the partition key.
     *
     * @param event The immutable order payload
     * @return CompletableFuture resolving to the Kafka SendResult
     */
    public CompletableFuture<SendResult<String, OrderCreatedEvent>> publishOrderEvent(OrderCreatedEvent event) {
        // Use productId as the partition key to guarantee sequential processing per product
        String partitionKey = event.productId();

        log.info("Publishing OrderCreatedEvent for orderId: {} on topic: {}", event.orderId(), FLASH_SALE_TOPIC);

        return kafkaTemplate.send(FLASH_SALE_TOPIC, partitionKey, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully published orderId: {} to partition: {} with offset: {}",
                                event.orderId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to publish orderId: {} due to error: {}", event.orderId(), ex.getMessage(), ex);
                    }
                });
    }

    /**
     * Java 21 Record for immutable order creation event contract.
     */
    public record OrderCreatedEvent(
            String orderId,
            String userId,
            String productId,
            int quantity,
            double totalAmount,
            Instant timestamp
    ) {}
}