package com.flashsale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling; // Added missing import

/**
 * Subsystem Name: Application Bootstrap Subsystem
 * Bounded Context: Flash Sale Domain - Execution Lifecycle
 * Responsibility: Initializes Spring Boot application context, package component scanning, and Kafka listener containers.
 */
@SpringBootApplication
@EnableKafka
@EnableScheduling
public class FlashSaleEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlashSaleEngineApplication.class, args);
    }
}