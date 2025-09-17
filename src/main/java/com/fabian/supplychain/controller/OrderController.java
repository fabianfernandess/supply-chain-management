package com.fabian.supplychain.controller;


import com.fabian.supplychain.dto.CreateOrderRequest;
import com.fabian.supplychain.entity.Order;
import com.fabian.supplychain.service.OrderService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling all Order-related API requests.
 */
@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Endpoint for creating a new order.
     * Uses @Valid to automatically trigger bean validation on the request body.
     *
     * @param request The order creation request DTO.
     * @return A ResponseEntity with the created order and HTTP status.
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Received request to create order for customer ID: {}", request.getCustomerId());
        Order createdOrder = orderService.createOrder(request);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }
}