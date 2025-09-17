package com.fabian.supplychain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The payload for the 'order-created' Kafka event.
 * Contains essential information about the newly created order.
 */
@Data
@NoArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private Long customerId;
    private LocalDateTime orderDate;
    private List<OrderItemPayload> items;

    @Data
    @NoArgsConstructor
    public static class OrderItemPayload {
        private Long productId;
        private Integer quantity;
    }
}
