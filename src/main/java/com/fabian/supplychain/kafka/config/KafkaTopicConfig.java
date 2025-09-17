package com.fabian.supplychain.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configuration for Kafka topics.
 * Defines the topics that our application will use.
 */
@Configuration
public class KafkaTopicConfig {

    public static final String ORDER_CREATED_TOPIC = "order-created";

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(ORDER_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}