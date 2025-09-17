package com.fabian.supplychain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a Customer Order.
 * Contains a list of OrderItems.
 */
@Entity
@Data
@NoArgsConstructor
public class Order implements Serializable {

    public enum Status {
        PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    // Many-to-one relationship with Customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    private Customer customer;

    // One-to-many relationship with OrderItem
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Shipment shipment;

    @PrePersist
    protected void onCreate() {
        this.orderDate = LocalDateTime.now();
        this.status = Status.PENDING;
    }
}