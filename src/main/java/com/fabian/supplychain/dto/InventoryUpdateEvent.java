package com.fabian.supplychain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The payload for the 'inventory-update' Kafka event.
 * Used to signal a change in product stock.
 */
@Data
@NoArgsConstructor
public class InventoryUpdateEvent {
    private Long productId;
    private Integer newStock;
    private Long version;
}