package com.flashsale.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public RecordMessageConverter jsonMessageConverter() {
        return new JsonMessageConverter();
    }

    @Bean
    public NewTopic flashSaleOrdersTopic() {
        return TopicBuilder.name("flash-sale-orders").partitions(3).replicas(1).build();
    }

    @Bean
    public ProducerFactory<String, String> stringProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> config = kafkaProperties.buildProducerProperties(null);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate(ProducerFactory<String, String> stringProducerFactory) {
        return new KafkaTemplate<>(stringProducerFactory);
    }
}
