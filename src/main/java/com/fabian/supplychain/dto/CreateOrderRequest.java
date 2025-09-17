package com.fabian.supplychain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * DTO for creating a new order.
 * Encapsulates the customer ID and the list of order items.
 */
@Data
public class CreateOrderRequest {
    @NotNull(message = "Customer ID cannot be null")
    private Long customerId;

    @NotNull(message = "Order items list cannot be null")
    @Size(min = 1, message = "Order must contain at least one item")
    @Valid
    private List<OrderItemDto> items;
}