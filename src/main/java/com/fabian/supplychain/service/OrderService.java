package com.fabian.supplychain.service;


import com.fabian.supplychain.dto.CreateOrderRequest;
import com.fabian.supplychain.dto.OrderCreatedEvent;
import com.fabian.supplychain.entity.Customer;
import com.fabian.supplychain.entity.Order;
import com.fabian.supplychain.entity.OrderItem;
import com.fabian.supplychain.entity.Product;
import com.fabian.supplychain.exception.ResourceNotFoundException;
import com.fabian.supplychain.exception.StockException;
import com.fabian.supplychain.kafka.producer.OrderEventProducer;
import com.fabian.supplychain.repository.CustomerRepository;
import com.fabian.supplychain.repository.OrderRepository;
import com.fabian.supplychain.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling all business logic related to orders.
 * It's responsible for creating orders and ensuring data consistency.
 */
@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderEventProducer orderEventProducer;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                        CustomerRepository customerRepository, OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.orderEventProducer = orderEventProducer;
    }

    /**
     * Creates a new order by reserving stock and saving the order details.
     * This method uses a transactional approach to ensure atomicity.
     * It's also @Retryable to handle optimistic locking failures.
     *
     * @param request The DTO containing the order details.
     * @return The created Order entity.
     * @throws ResourceNotFoundException if customer or product is not found.
     * @throws StockException if there is insufficient stock.
     * @throws ObjectOptimisticLockingFailureException if a concurrent update occurs.
     */
    @Transactional
    @Retryable(
            retryFor = { ObjectOptimisticLockingFailureException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public Order createOrder(CreateOrderRequest request) {
        // 1. Fetch customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));

        Order order = new Order();
        order.setCustomer(customer);

        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemDto -> {
                    // 2. Fetch and update product stock
                    Product product = productRepository.findById(itemDto.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemDto.getProductId()));

                    if (product.getStock() < itemDto.getQuantity()) {
                        throw new StockException("Insufficient stock for product: " + product.getName());
                    }

                    // Decrement stock and save. This is where optimistic locking is triggered.
                    product.setStock(product.getStock() - itemDto.getQuantity());
                    productRepository.save(product);

                    // 3. Create OrderItem entity
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProduct(product);
                    orderItem.setQuantity(itemDto.getQuantity());
                    orderItem.setPrice(itemDto.getPrice());
                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);

        // 4. Save the order to the database
        Order savedOrder = orderRepository.save(order);

        // 5. Publish Kafka event
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(savedOrder.getId());
        event.setCustomerId(savedOrder.getCustomer().getId());
        event.setOrderDate(savedOrder.getOrderDate());
        event.setItems(savedOrder.getOrderItems().stream()
                .map(item -> {
                    OrderCreatedEvent.OrderItemPayload payload = new OrderCreatedEvent.OrderItemPayload();
                    payload.setProductId(item.getProduct().getId());
                    payload.setQuantity(item.getQuantity());
                    return payload;
                })
                .collect(Collectors.toList()));

        orderEventProducer.sendOrderCreatedEvent(event);
        log.info("Successfully created order and published event for order ID: {}", savedOrder.getId());

        return savedOrder;
    }
}
