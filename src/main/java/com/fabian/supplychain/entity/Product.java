package com.fabian.supplychain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Represents a Product in the supply chain.
 * Uses optimistic locking to prevent concurrent updates on inventory.
 */
@Entity
@Data
@NoArgsConstructor
@ToString(exclude = "orderItems")
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name cannot be blank")
    private String name;

    @NotBlank(message = "SKU cannot be blank")
    @Column(unique = true)
    private String sku;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Current stock cannot be null")
    @DecimalMin(value = "0", message = "Stock count cannot be negative")
    private Integer stock;

    @Version
    private Long version;

    // Unidirectional relationship to prevent circular dependency issues
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;
}
