package com.fabian.supplychain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents the shipment of an Order.
 */
@Entity
@Data
@NoArgsConstructor
public class Shipment implements Serializable {

    public enum Status {
        DISPATCHED, IN_TRANSIT, DELIVERED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dispatchedDate;
    private LocalDateTime deliveryDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    // One-to-one relationship with Order
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true, nullable = false)
    private Order order;
}
