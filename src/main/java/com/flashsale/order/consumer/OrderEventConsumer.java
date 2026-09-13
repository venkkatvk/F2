package com.flashsale.order.consumer;

import com.flashsale.order.event.OrderEventProducer.OrderCreatedEvent;
import com.flashsale.telemetry.TelemetrySseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final OrderRepository orderRepository;
    private final TelemetrySseController telemetrySseController;

    public OrderEventConsumer(OrderRepository orderRepository, TelemetrySseController telemetrySseController) {
        this.orderRepository = orderRepository;
        this.telemetrySseController = telemetrySseController;
    }

    @KafkaListener(topics = "flash-sale-orders")
    @Transactional
    public void consumeOrderEvent(OrderCreatedEvent event, Acknowledgment ack) {
        orderRepository.findById(event.orderId()).ifPresentOrElse(order -> {
            if ("CONFIRMED".equals(order.getStatus())) {
                ack.acknowledge();
                return;
            }
            order.setStatus("CONFIRMED");
            orderRepository.save(order);
            telemetrySseController.broadcastTelemetryEvent(
                    "Order confirmed: " + event.orderId() + " for product " + event.productId()
            );
            ack.acknowledge();
        }, () -> {
            log.warn("Received Kafka event for unknown orderId: {}", event.orderId());
            ack.acknowledge();
        });
    }
}
