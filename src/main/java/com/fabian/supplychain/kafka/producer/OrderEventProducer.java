package com.fabian.supplychain.kafka.producer;

import com.fabian.supplychain.dto.OrderCreatedEvent;

import com.fabian.supplychain.kafka.config.KafkaTopicConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service to produce Kafka events.
 * It's responsible for sending messages to a Kafka topic.
 */
@Service
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Sends an OrderCreatedEvent to the 'order-created' topic.
     * @param event The event payload.
     */
    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Sending OrderCreatedEvent for order ID: {}", event.getOrderId());
        kafkaTemplate.send(KafkaTopicConfig.ORDER_CREATED_TOPIC, String.valueOf(event.getOrderId()), event);
    }
}