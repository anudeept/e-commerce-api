package com.ecommerce.shoppingcart;

import static org.junit.jupiter.api.Assertions.*;

import com.ecommerce.auth.dto.JwtAuthResponse;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.product.ProductEntity;
import com.ecommerce.product.ProductRepository;
import com.ecommerce.shoppingcart.dto.CartItemRequestDto;
import com.ecommerce.shoppingcart.dto.ShoppingCartResponseDto;
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
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ShoppingCartIntegrationTest {

  @Container static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.6");
  private static final String BASE_URL = "/api/v1/ecommerce";
  private static final String AUTH_URL = BASE_URL + "/auth";
  private static final String USERS_URL = BASE_URL + "/users";
  private static final String CART_URL = BASE_URL + "/shopping-carts";
  private static final String PRODUCTS_URL = BASE_URL + "/products";

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private ShoppingCartRepository cartRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private ProductRepository productRepository;

  private HttpHeaders headers;
  private String userId;
  private String productId;

  @BeforeEach
  void setUp() {
    cartRepository.deleteAll();
    userRepository.deleteAll();
    productRepository.deleteAll();

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Create admin user using API
    UserRequestDto userRequestDto = new UserRequestDto();
    userRequestDto.setEmail("admin@example.com");
    userRequestDto.setPassword("Admin123#");
    userRequestDto.setFirstName("Admin");
    userRequestDto.setLastName("User");
    userRequestDto.setRole(Role.ROLE_ADMIN);

    ResponseEntity<UserResponseDto> registerResponse =
        restTemplate.postForEntity(
            AUTH_URL + "/register",
            new HttpEntity<>(userRequestDto, headers),
            UserResponseDto.class);

    userId = registerResponse.getBody().getId();

    // Authenticate admin user and set headers
    authenticateUserAndSetHeaders("admin@example.com", "Admin123#");

    // Create test product
    ProductEntity product = new ProductEntity();
    product.setName("Test Product");
    product.setPrice(99.99);
    product.setCategory("Electronics");
    product.setStockQuantity(10);
    productId = productRepository.save(product).getId();
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
  void createCart_Success() {
    ResponseEntity<ShoppingCartResponseDto> response =
        restTemplate.postForEntity(
            CART_URL + "?userId=" + userId,
            new HttpEntity<>(headers),
            ShoppingCartResponseDto.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getId());
    assertTrue(response.getBody().getItems().isEmpty());
  }

  @Test
  void getCart_Success() {
    // First create a cart
    String cartId = createTestCart();

    ResponseEntity<ShoppingCartResponseDto> response =
        restTemplate.exchange(
            CART_URL + "/" + cartId,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ShoppingCartResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(cartId, response.getBody().getId());
  }

  @Test
  void addItemToCart_Success() {
    // First create a cart
    String cartId = createTestCart();

    // Create item request
    CartItemRequestDto itemRequest = new CartItemRequestDto();
    itemRequest.setProductId(productId);
    itemRequest.setQuantity(2);

    ResponseEntity<ShoppingCartResponseDto> response =
        restTemplate.exchange(
            CART_URL + "/" + cartId + "/items",
            HttpMethod.POST,
            new HttpEntity<>(itemRequest, headers),
            ShoppingCartResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getItems().size());
    assertEquals(productId, response.getBody().getItems().get(0).getProductId());
    assertEquals(2, response.getBody().getItems().get(0).getQuantity());
  }

  @Test
  void updateItemQuantity_Success() {
    // First create a cart with an item
    String cartId = createTestCartWithItem();

    // Update quantity
    CartItemRequestDto updateRequest = new CartItemRequestDto();
    updateRequest.setProductId(productId);
    updateRequest.setQuantity(5);

    ResponseEntity<ShoppingCartResponseDto> response =
        restTemplate.exchange(
            CART_URL + "/" + cartId + "/items",
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest, headers),
            ShoppingCartResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getItems().size());
    assertEquals(5, response.getBody().getItems().get(0).getQuantity());
  }

  @Test
  void removeItemFromCart_Success() {
    // First create a cart with an item
    String cartId = createTestCartWithItem();

    ResponseEntity<ShoppingCartResponseDto> response =
        restTemplate.exchange(
            CART_URL + "/" + cartId + "/items/" + productId,
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            ShoppingCartResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().getItems().isEmpty());
  }

  @Test
  void clearCart_Success() {
    // First create a cart with an item
    String cartId = createTestCartWithItem();

    ResponseEntity<ShoppingCartResponseDto> response =
        restTemplate.exchange(
            CART_URL + "/" + cartId + "/items",
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            ShoppingCartResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().getItems().isEmpty());
  }

  @Test
  void deleteCart_Success() {
    // First create a cart
    String cartId = createTestCart();

    restTemplate.exchange(
        CART_URL + "/" + cartId, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);

    ResponseEntity<ShoppingCartResponseDto> getResponse =
        restTemplate.exchange(
            CART_URL + "/" + cartId,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ShoppingCartResponseDto.class);
    assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
  }

  @Test
  void addItemToCart_ExceedsStock_Fails() {
    // First create a cart
    String cartId = createTestCart();

    // Try to add more items than available in stock
    CartItemRequestDto itemRequest = new CartItemRequestDto();
    itemRequest.setProductId(productId);
    itemRequest.setQuantity(20); // Stock is only 10

    ResponseEntity<ShoppingCartResponseDto> response =
        restTemplate.exchange(
            CART_URL + "/" + cartId + "/items",
            HttpMethod.POST,
            new HttpEntity<>(itemRequest, headers),
            ShoppingCartResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void addItemToCart_ExistingItem_UpdatesQuantity() {
    // First create a cart
    String cartId = createTestCart();

    // Add item first time
    CartItemRequestDto firstRequest = new CartItemRequestDto();
    firstRequest.setProductId(productId);
    firstRequest.setQuantity(2);

    restTemplate.exchange(
        CART_URL + "/" + cartId + "/items",
        HttpMethod.POST,
        new HttpEntity<>(firstRequest, headers),
        ShoppingCartResponseDto.class);

    // Add same item second time
    CartItemRequestDto secondRequest = new CartItemRequestDto();
    secondRequest.setProductId(productId);
    secondRequest.setQuantity(3);

    ResponseEntity<ShoppingCartResponseDto> response =
        restTemplate.exchange(
            CART_URL + "/" + cartId + "/items",
            HttpMethod.POST,
            new HttpEntity<>(secondRequest, headers),
            ShoppingCartResponseDto.class);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getItems().size()); // Still only one item
    assertEquals(5, response.getBody().getItems().get(0).getQuantity()); // 2 + 3 = 5
    assertEquals(productId, response.getBody().getItems().get(0).getProductId());
  }

  @Test
  void addItemToCart_ExistingItem_ExceedsStock_Fails() {
    // First create a cart
    String cartId = createTestCart();

    // Add item first time
    CartItemRequestDto firstRequest = new CartItemRequestDto();
    firstRequest.setProductId(productId);
    firstRequest.setQuantity(7); // First add 7

    restTemplate.exchange(
        CART_URL + "/" + cartId + "/items",
        HttpMethod.POST,
        new HttpEntity<>(firstRequest, headers),
        ShoppingCartResponseDto.class);

    // Try to add more items that would exceed stock
    CartItemRequestDto secondRequest = new CartItemRequestDto();
    secondRequest.setProductId(productId);
    secondRequest.setQuantity(4); // Try to add 4 more (7 + 4 > 10 stock)

    ResponseEntity<ShoppingCartResponseDto> response =
        restTemplate.exchange(
            CART_URL + "/" + cartId + "/items",
            HttpMethod.POST,
            new HttpEntity<>(secondRequest, headers),
            ShoppingCartResponseDto.class);

    // Then
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    // Verify original quantity remains unchanged
    ResponseEntity<ShoppingCartResponseDto> getResponse =
        restTemplate.exchange(
            CART_URL + "/" + cartId,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ShoppingCartResponseDto.class);
    assertEquals(7, getResponse.getBody().getItems().get(0).getQuantity());
  }

  private String createTestCart() {
    ResponseEntity<ShoppingCartResponseDto> response =
        restTemplate.postForEntity(
            CART_URL + "?userId=" + userId,
            new HttpEntity<>(headers),
            ShoppingCartResponseDto.class);
    return response.getBody().getId();
  }

  private String createTestCartWithItem() {
    String cartId = createTestCart();

    CartItemRequestDto itemRequest = new CartItemRequestDto();
    itemRequest.setProductId(productId);
    itemRequest.setQuantity(2);

    restTemplate.exchange(
        CART_URL + "/" + cartId + "/items",
        HttpMethod.POST,
        new HttpEntity<>(itemRequest, headers),
        ShoppingCartResponseDto.class);

    return cartId;
  }
}
