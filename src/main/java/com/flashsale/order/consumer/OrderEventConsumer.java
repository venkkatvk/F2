package com.flashsale.order.consumer;

import com.flashsale.order.event.OrderEventProducer.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Subsystem Name: Order Consumer & Persistence Subsystem
 * Bounded Context: Flash Sale Domain - Asynchronous Order Finalization
 * Responsibility: Listens to Kafka order streams and guarantees database persistence in PostgreSQL.
 */
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final OrderRepository orderRepository;

    public OrderEventConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Consumes order events from Kafka consumer group and writes them to PostgreSQL.
     *
     * @param event          Deserialized immutable order payload from Kafka
     * @param acknowledgment Manual offset commit handle to guarantee at-least-once delivery
     */
    @KafkaListener(
            topics = "flash-sale-orders",
            groupId = "flash-sale-order-processors",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeOrderEvent(OrderCreatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received OrderCreatedEvent from Kafka for processing: orderId={}", event.orderId());

        try {
            // Map event payload into persistent JPA Entity
            OrderEntity orderEntity = new OrderEntity(
                    event.orderId(),
                    event.userId(),
                    event.productId(),
                    event.quantity(),
                    event.totalAmount(),
                    "CONFIRMED",
                    event.timestamp()
            );

            // Persist order to PostgreSQL
            orderRepository.save(orderEntity);

            // Manually commit Kafka offset ONLY after database commit succeeds
            acknowledgment.acknowledge();

            log.info("Order successfully persisted and Kafka offset committed: orderId={}", event.orderId());

        } catch (Exception ex) {
            log.error("Failed to process order event: orderId={}. Triggering retry pipeline.", event.orderId(), ex);
            // Re-throw exception to allow Spring Kafka error handler / DLQ (Dead Letter Queue) processing
            throw ex;
        }
    }
}