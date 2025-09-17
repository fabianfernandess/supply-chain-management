package com.fabian.supplychain.controller;


import com.fabian.supplychain.entity.Product;
import com.fabian.supplychain.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling Product-related API requests.
 * Demonstrates pagination.
 */
@RestController
@RequestMapping("/api/products")
@Slf4j
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Endpoint to retrieve a paginated list of all products.
     * Spring Data JPA handles the pagination logic automatically with the Pageable parameter.
     * Example URL: /api/products?page=0&size=10&sort=name,asc
     *
     * @param pageable The pagination information provided by Spring.
     * @return A Page object containing a list of products and pagination metadata.
     */
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(Pageable pageable) {
        log.info("Fetching products with pageable: {}", pageable);
        Page<Product> productPage = productRepository.findAll(pageable);
        return ResponseEntity.ok(productPage);
    }
}
