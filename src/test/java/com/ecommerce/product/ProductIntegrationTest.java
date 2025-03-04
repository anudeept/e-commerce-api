package com.ecommerce.product;

import static org.junit.jupiter.api.Assertions.*;

import com.ecommerce.auth.dto.JwtAuthResponse;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.common.ErrorResponse;
import com.ecommerce.common.PageResponse;
import com.ecommerce.product.dto.ProductRequestDto;
import com.ecommerce.users.Role;
import com.ecommerce.users.UserRepository;
import com.ecommerce.users.dto.UserRequestDto;
import com.ecommerce.users.dto.UserResponseDto;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductIntegrationTest {

  @Container static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.6");
  private static final String BASE_URL = "/api/v1/ecommerce";
  private static final String AUTH_URL = BASE_URL + "/auth";
  private static final String PRODUCTS_URL = BASE_URL + "/products";

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private ProductRepository productRepository;

  @Autowired private UserRepository userRepository;

  private ProductRequestDto productRequestDto;
  private HttpHeaders headers;

  @BeforeEach
  void setUp() {
    productRepository.deleteAll();
    userRepository.deleteAll();

    // Create admin user
    UserRequestDto userRequestDto = new UserRequestDto();
    userRequestDto.setEmail("admin@example.com");
    userRequestDto.setPassword("Admin123#");
    userRequestDto.setFirstName("Admin");
    userRequestDto.setLastName("User");
    userRequestDto.setRole(Role.ROLE_ADMIN);

    ResponseEntity<UserResponseDto> registerResponse =
        restTemplate.postForEntity(
            AUTH_URL + "/register", new HttpEntity<>(userRequestDto), UserResponseDto.class);

    // Set up product request
    productRequestDto = new ProductRequestDto();
    productRequestDto.setName("Test Product");
    productRequestDto.setDescription("Test Description");
    productRequestDto.setPrice(99.99);
    productRequestDto.setCategory("Electronics");
    productRequestDto.setStockQuantity(10);
    productRequestDto.setActive(true);

    // Set up headers with authentication
    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Authenticate and set JWT token
    authenticateUserAndSetHeaders("admin@example.com", "Admin123#");
  }

  private void authenticateUserAndSetHeaders(String email, String password) {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail(email);
    loginRequest.setPassword(password);

    ResponseEntity<JwtAuthResponse> loginResponse =
        restTemplate.postForEntity(
            AUTH_URL + "/login", new HttpEntity<>(loginRequest, headers), JwtAuthResponse.class);

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(Objects.requireNonNull(loginResponse.getBody()).getAccessToken());
  }

  @Test
  void createProduct_Success() {
    // Given
    productRequestDto.setPrice(99.99); // Valid price

    // When
    ResponseEntity<ProductEntity> response =
        restTemplate.postForEntity(
            PRODUCTS_URL, new HttpEntity<>(productRequestDto, headers), ProductEntity.class);

    // Then
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getId());
    assertEquals(productRequestDto.getName(), response.getBody().getName());
    assertEquals(productRequestDto.getPrice(), response.getBody().getPrice());
  }

  @Test
  void getProductById_Success() {
    // Given
    ProductEntity savedProduct =
        restTemplate
            .postForEntity(
                PRODUCTS_URL, new HttpEntity<>(productRequestDto, headers), ProductEntity.class)
            .getBody();

    // When
    ResponseEntity<ProductEntity> response =
        restTemplate.exchange(
            PRODUCTS_URL + "/" + savedProduct.getId(),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ProductEntity.class);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(savedProduct.getId(), response.getBody().getId());
  }

  @Test
  void getAllProducts_WithFilters_Success() {
    // Given
    createSampleProducts();

    // When
    ResponseEntity<PageResponse<ProductEntity>> response =
        restTemplate.exchange(
            PRODUCTS_URL + "?category=Electronics&minPrice=50&maxPrice=150",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<PageResponse<ProductEntity>>() {});

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void updateProduct_Success() {
    // Given
    ProductEntity savedProduct =
        restTemplate
            .postForEntity(
                PRODUCTS_URL, new HttpEntity<>(productRequestDto, headers), ProductEntity.class)
            .getBody();

    ProductRequestDto updateRequest = new ProductRequestDto();
    updateRequest.setName("Updated Product");
    updateRequest.setPrice(149.99);
    updateRequest.setCategory("Electronics");
    updateRequest.setDescription("Updated Description");
    updateRequest.setStockQuantity(20);
    updateRequest.setActive(true);

    // When
    ResponseEntity<ProductEntity> response =
        restTemplate.exchange(
            PRODUCTS_URL + "/" + savedProduct.getId(),
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest, headers),
            ProductEntity.class);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Updated Product", response.getBody().getName());
    assertEquals(149.99, response.getBody().getPrice());
  }

  //    @Test
  //    void partialUpdateProduct_Success() {
  //      // Given
  //      ProductEntity savedProduct =
  //          restTemplate
  //              .postForEntity(
  //                  "/api/products", new HttpEntity<>(productRequestDto, headers),
  //   ProductEntity.class)
  //              .getBody();
  //
  //      ProductRequestDto partialUpdate = new ProductRequestDto();
  //      partialUpdate.setPrice(79.99);
  //
  //      // When
  //      ResponseEntity<ProductEntity> response =
  //          restTemplate.exchange(
  //              "/api/products/" + savedProduct.getId(),
  //              HttpMethod.PATCH,
  //              new HttpEntity<>(partialUpdate, headers),
  //              ProductEntity.class);
  //
  //      // Then
  //      assertEquals(HttpStatus.OK, response.getStatusCode());
  //      assertNotNull(response.getBody());
  //      assertEquals(
  //          savedProduct.getName(), response.getBody().getName()); //   Name should remain
  // unchanged
  //      assertEquals(79.99, response.getBody().getPrice());
  //    }

  @Test
  void deleteProduct_Success() {
    // Given
    ProductEntity savedProduct =
        restTemplate
            .postForEntity(
                PRODUCTS_URL, new HttpEntity<>(productRequestDto, headers), ProductEntity.class)
            .getBody();

    // When
    restTemplate.exchange(
        PRODUCTS_URL + "/" + savedProduct.getId(),
        HttpMethod.DELETE,
        new HttpEntity<>(headers),
        Void.class);

    // Then
    ResponseEntity<ProductEntity> response =
        restTemplate.exchange(
            PRODUCTS_URL + "/" + savedProduct.getId(),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ProductEntity.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void createProduct_InvalidPrice_Fails() {
    // Given
    productRequestDto.setPrice(-10.0); // Invalid negative price

    // When
    ResponseEntity<ErrorResponse> response =
        restTemplate.postForEntity(
            PRODUCTS_URL, new HttpEntity<>(productRequestDto, headers), ErrorResponse.class);

    // Then
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  private void createSampleProducts() {
    // Create multiple products for testing filters
    ProductRequestDto product1 = new ProductRequestDto();
    product1.setName("Test Product 1");
    product1.setPrice(99.99);
    product1.setCategory("Electronics");
    restTemplate.postForEntity(
        PRODUCTS_URL, new HttpEntity<>(product1, headers), ProductEntity.class);

    ProductRequestDto product2 = new ProductRequestDto();
    product2.setName("Test Product 2");
    product2.setPrice(149.99);
    product2.setCategory("Electronics");
    restTemplate.postForEntity(
        PRODUCTS_URL, new HttpEntity<>(product2, headers), ProductEntity.class);

    ProductRequestDto product3 = new ProductRequestDto();
    product3.setName("Different Product");
    product3.setPrice(49.99);
    product3.setCategory("Books");
    restTemplate.postForEntity(
        PRODUCTS_URL, new HttpEntity<>(product3, headers), ProductEntity.class);
  }
}
