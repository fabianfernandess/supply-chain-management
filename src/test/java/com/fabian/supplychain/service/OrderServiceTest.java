package com.fabian.supplychain.service;

import com.fabian.supplychain.dto.CreateOrderRequest;
import com.fabian.supplychain.dto.OrderItemDto;
import com.fabian.supplychain.entity.Customer;
import com.fabian.supplychain.entity.Product;
import com.fabian.supplychain.exception.StockException;
import com.fabian.supplychain.kafka.producer.OrderEventProducer;
import com.fabian.supplychain.repository.CustomerRepository;
import com.fabian.supplychain.repository.OrderRepository;
import com.fabian.supplychain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private Product product;
    private CreateOrderRequest validRequest;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);

        product = new Product();
        product.setId(1L);
        product.setStock(10);
        product.setPrice(BigDecimal.valueOf(100));
        product.setVersion(0L);

        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);
        itemDto.setPrice(BigDecimal.valueOf(100));

        validRequest = new CreateOrderRequest();
        validRequest.setCustomerId(1L);
        validRequest.setItems(List.of(itemDto));
    }

    @Test
    void whenCreateOrder_thenOrderIsSavedAndEventIsSent() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any())).thenReturn(any());

        orderService.createOrder(validRequest);

        verify(orderRepository, times(1)).save(any());
        verify(productRepository, times(1)).save(any());
        verify(orderEventProducer, times(1)).sendOrderCreatedEvent(any());
    }

    @Test
    void whenCreateOrderWithInsufficientStock_thenThrowStockException() {
        product.setStock(1);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(StockException.class, () -> orderService.createOrder(validRequest));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void whenCreateOrderWithOptimisticLockingFailure_thenRetrySucceeds() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException("Simulated locking failure", null))
                .thenReturn(product); // Succeed on the second attempt
        when(orderRepository.save(any())).thenReturn(any());

        orderService.createOrder(validRequest);

        verify(productRepository, times(2)).save(any());
        verify(orderRepository, times(1)).save(any());
    }
}
