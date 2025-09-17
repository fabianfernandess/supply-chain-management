package com.fabian.supplychain.service;

import com.fabian.supplychain.dto.InventoryUpdateEvent;
import com.fabian.supplychain.entity.Product;

import com.fabian.supplychain.exception.ResourceNotFoundException;
import com.fabian.supplychain.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling inventory-related business logic.
 * This service can be used for manual stock updates or by a Kafka consumer.
 */
@Service
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Updates the stock of a product.
     * This method is transactional and will be called by our Kafka consumer
     * to reflect stock changes from external events.
     *
     * @param event The InventoryUpdateEvent payload.
     * @throws ResourceNotFoundException if the product is not found.
     * @throws ObjectOptimisticLockingFailureException if a concurrent update occurs.
     */
    @Transactional
    public void updateStock(InventoryUpdateEvent event) {
        log.info("Attempting to update stock for product ID: {}", event.getProductId());
        try {
            Product product = productRepository.findById(event.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + event.getProductId()));

            // Check if the version matches to prevent stale data updates.
            // This is an additional safeguard on top of JPA's built-in locking.
            if (!product.getVersion().equals(event.getVersion())) {
                log.warn("Stale data detected for product ID: {}. Expected version: {}, found version: {}",
                        product.getId(), product.getVersion(), event.getVersion());
                throw new ObjectOptimisticLockingFailureException(Product.class, product.getId());
            }

            product.setStock(event.getNewStock());
            productRepository.save(product);
            log.info("Successfully updated stock for product ID: {} to {}", product.getId(), product.getStock());

        } catch (ObjectOptimisticLockingFailureException e) {
            log.error("Optimistic locking failure for product ID: {}. Retrying might be needed.", event.getProductId(), e);
            throw e; // Re-throw to allow a retry mechanism to handle it
        }
    }
}