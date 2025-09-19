package com.fabian.supplychain;

import com.fabian.supplychain.dto.CreateOrderRequest;
import com.fabian.supplychain.dto.OrderItemDto;
import com.fabian.supplychain.entity.Customer;
import com.fabian.supplychain.entity.Order;
import com.fabian.supplychain.entity.Product;
import com.fabian.supplychain.repository.CustomerRepository;
import com.fabian.supplychain.repository.OrderRepository;
import com.fabian.supplychain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the entire application flow.
 * Uses Testcontainers to run a real MySQL database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers // Correct annotation to enable Testcontainers
@ActiveProfiles("test")
class SupplyChainMonolithApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private OrderRepository orderRepository;

	// Correct annotation for the container
	@Container
	private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.27");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", mysqlContainer::getUsername);
		registry.add("spring.datasource.password", mysqlContainer::getPassword);
		registry.add("spring.kafka.bootstrap-servers", () -> "not-a-real-server:9092");
	}

	private Product testProduct;
	private Customer testCustomer;

	@BeforeEach
	void setUp() {
		orderRepository.deleteAll();
		productRepository.deleteAll();
		customerRepository.deleteAll();

		testCustomer = new Customer();
		testCustomer.setName("John Doe");
		testCustomer.setEmail("john.doe@test.com");
		testCustomer.setAddress("123 Test St");
		customerRepository.save(testCustomer);

		testProduct = new Product();
		testProduct.setName("Test Product");
		testProduct.setSku("TP-001");
		testProduct.setPrice(BigDecimal.valueOf(99.99));
		testProduct.setStock(50);
		productRepository.save(testProduct);
	}

	@Test
	void contextLoads() {
		assertThat(restTemplate).isNotNull();
	}

	@Test
	void whenCreateOrderApiCalled_thenOrderIsCreatedAndStockIsUpdated() {
		OrderItemDto itemDto = new OrderItemDto();
		itemDto.setProductId(testProduct.getId());
		itemDto.setQuantity(2);
		itemDto.setPrice(BigDecimal.valueOf(99.99));

		CreateOrderRequest request = new CreateOrderRequest();
		request.setCustomerId(testCustomer.getId());
		request.setItems(List.of(itemDto));

		String url = "http://localhost:" + port + "/api/orders";

		ResponseEntity<Order> response = restTemplate.postForEntity(url, request, Order.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();

		Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
		assertThat(updatedProduct.getStock()).isEqualTo(48);
	}
}