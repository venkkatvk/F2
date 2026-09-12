package com.flashsale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Subsystem Name: Application Bootstrap Subsystem
 * Bounded Context: Flash Sale Domain - Execution Lifecycle
 * Responsibility: Initializes Spring Boot application context, package component scanning, and Kafka listener containers.
 */
@SpringBootApplication
@EnableKafka
public class FlashSaleEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlashSaleEngineApplication.class, args);
    }
}