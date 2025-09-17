package com.fabian.supplychain.service;

import com.fabian.supplychain.entity.Order;
import com.fabian.supplychain.entity.Shipment;
import com.fabian.supplychain.exception.ResourceNotFoundException;
import com.fabian.supplychain.repository.OrderRepository;
import com.fabian.supplychain.repository.ShipmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for handling all business logic related to shipments.
 */
@Service
@Slf4j
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;

    public ShipmentService(ShipmentRepository shipmentRepository, OrderRepository orderRepository) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Creates a new shipment record for a given order.
     * This method is triggered by the Kafka consumer.
     * @param orderId The ID of the order to create a shipment for.
     */
    @Transactional
    public void createShipmentForOrder(Long orderId) {
        log.info("Creating shipment for order ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setDispatchedDate(LocalDateTime.now());
        shipment.setStatus(Shipment.Status.DISPATCHED);

        shipmentRepository.save(shipment);
        log.info("Shipment created for order ID: {}", orderId);
    }
}
