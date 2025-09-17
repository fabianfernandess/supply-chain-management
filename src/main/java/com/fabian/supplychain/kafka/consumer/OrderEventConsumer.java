package com.fabian.supplychain.kafka.consumer;

import com.fabian.supplychain.dto.OrderCreatedEvent;
import com.fabian.supplychain.kafka.config.KafkaTopicConfig;
import com.fabian.supplychain.service.ShipmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka Consumer for handling events related to orders.
 */
@Service
@Slf4j
public class OrderEventConsumer {

    private final ShipmentService shipmentService;

    public OrderEventConsumer(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    /**
     * Listens to the 'order-created' topic.
     * This method is automatically handled by a separate thread managed by Spring's KafkaListener.
     *
     * @param event The OrderCreatedEvent payload.
     */
    @KafkaListener(topics = KafkaTopicConfig.ORDER_CREATED_TOPIC, groupId = "shipment-group")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received new OrderCreatedEvent for order ID: {} from Kafka. Processing...", event.getOrderId());
        // Simulate a business process triggered by the event, e.g., creating a shipment record.
        shipmentService.createShipmentForOrder(event.getOrderId());
        log.info("Successfully processed OrderCreatedEvent for order ID: {}", event.getOrderId());
    }
}
