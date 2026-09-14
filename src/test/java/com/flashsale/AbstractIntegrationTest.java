package com.flashsale;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    // Satisfies OrderEventProducer dependency injection during test context loading
    @MockBean
    @SuppressWarnings("rawtypes")
    private KafkaTemplate kafkaTemplate;
}