package com.fabian.supplychain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service to handle notifications.
 * The methods are marked as @Async to run on a separate thread,
 * preventing them from blocking the main request thread.
 */
@Service
@Slf4j
public class NotificationService {

    /**
     * Simulates sending a confirmation email asynchronously.
     * The @Async annotation offloads this task to a separate thread pool.
     * @param orderId The ID of the order.
     */
    @Async("taskExecutor")
    public void sendOrderConfirmationEmail(Long orderId) {
        try {
            log.info("Starting email notification for order ID: {}. Current thread: {}", orderId, Thread.currentThread().getName());
            // Simulate a time-consuming operation, like an API call to a mailing service.
            Thread.sleep(5000);
            log.info("Email sent successfully for order ID: {}", orderId);
        } catch (InterruptedException e) {
            log.error("Email sending interrupted for order ID: {}", orderId, e);
            Thread.currentThread().interrupt();
        }
    }
}
