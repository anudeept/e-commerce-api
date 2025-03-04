package com.ecommerce.users;

import static org.junit.jupiter.api.Assertions.*;

import com.ecommerce.auth.dto.JwtAuthResponse;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.common.PageResponse;
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
class UserIntegrationTest {

  @Container static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.6");
  private static final String BASE_URL = "/api/v1/ecommerce";
  private static final String AUTH_URL = BASE_URL + "/auth";
  private static final String USERS_URL = BASE_URL + "/users";

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private UserRepository userRepository;

  private UserRequestDto adminRequestDto;
  private UserRequestDto customerRequestDto;
  private HttpHeaders headers;
  private HttpHeaders adminHeaders;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Setup admin request dto
    adminRequestDto = new UserRequestDto();
    adminRequestDto.setEmail("admin@example.com");
    adminRequestDto.setPassword("Admin123#");
    adminRequestDto.setFirstName("Admin");
    adminRequestDto.setLastName("User");
    adminRequestDto.setRole(Role.ROLE_ADMIN);

    // Setup customer request dto
    customerRequestDto = new UserRequestDto();
    customerRequestDto.setEmail("customer@example.com");
    customerRequestDto.setPassword("Customer123#");
    customerRequestDto.setFirstName("Test");
    customerRequestDto.setLastName("Customer");
    customerRequestDto.setRole(Role.ROLE_CUSTOMER);

    // Register and login as admin
    setupAdminAndGetAuthHeader("Admin123#");
  }

  private void setupAdminAndGetAuthHeader(String adminPassword) {
    // Register admin
    ResponseEntity<UserResponseDto> adminResponse =
        restTemplate.postForEntity(
            AUTH_URL + "/register",
            new HttpEntity<>(adminRequestDto, headers),
            UserResponseDto.class);

    // Login as admin
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail(adminRequestDto.getEmail());
    loginRequest.setPassword(adminPassword);

    ResponseEntity<JwtAuthResponse> loginResponse =
        restTemplate.postForEntity(
            AUTH_URL + "/login", new HttpEntity<>(loginRequest, headers), JwtAuthResponse.class);

    // Set up admin headers
    adminHeaders = new HttpHeaders();
    adminHeaders.setContentType(MediaType.APPLICATION_JSON);
    adminHeaders.setBearerAuth(Objects.requireNonNull(loginResponse.getBody()).getAccessToken());
  }

  @Test
  void registerSecondAdmin_Fails() {
    // Try to register second admin
    UserRequestDto secondAdminRequest = new UserRequestDto();
    secondAdminRequest.setEmail("admin2@example.com");
    secondAdminRequest.setPassword("Admin123#");
    secondAdminRequest.setFirstName("Second");
    secondAdminRequest.setLastName("Admin");
    secondAdminRequest.setRole(Role.ROLE_ADMIN);

    ResponseEntity<UserResponseDto> response =
        restTemplate.postForEntity(
            AUTH_URL + "/register",
            new HttpEntity<>(secondAdminRequest, headers),
            UserResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(1, userRepository.findByRolesContaining(Role.ROLE_ADMIN).size());
  }

  @Test
  void registerCustomer_Success() {
    ResponseEntity<UserResponseDto> response =
        restTemplate.postForEntity(
            AUTH_URL + "/register",
            new HttpEntity<>(customerRequestDto, headers),
            UserResponseDto.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(customerRequestDto.getEmail(), response.getBody().getEmail());
    assertTrue(response.getBody().getRoles().contains(Role.ROLE_CUSTOMER.name()));
  }

  @Test
  void getAllUsers_AdminAccess_Success() {
    // Register a customer
    restTemplate.postForEntity(
        AUTH_URL + "/register",
        new HttpEntity<>(customerRequestDto, headers),
        UserResponseDto.class);

    ResponseEntity<PageResponse<UserResponseDto>> response =
        restTemplate.exchange(
            USERS_URL + "?page=0&size=10",
            HttpMethod.GET,
            new HttpEntity<>(adminHeaders),
            new ParameterizedTypeReference<PageResponse<UserResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().getTotalElements()); // Admin + Customer
  }

  @Test
  void getAllUsers_CustomerAccess_Forbidden() {
    // Register and login as customer
    restTemplate.postForEntity(
        AUTH_URL + "/register",
        new HttpEntity<>(customerRequestDto, headers),
        UserResponseDto.class);

    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail(customerRequestDto.getEmail());
    loginRequest.setPassword(customerRequestDto.getPassword());

    ResponseEntity<JwtAuthResponse> loginResponse =
        restTemplate.postForEntity(
            AUTH_URL + "/login",
            new HttpEntity<>(loginRequest, new HttpHeaders()),
            JwtAuthResponse.class);

    HttpHeaders customerHeaders = new HttpHeaders();
    customerHeaders.setBearerAuth(loginResponse.getBody().getAccessToken());

    ResponseEntity<PageResponse<UserResponseDto>> response =
        restTemplate.exchange(
            USERS_URL + "?page=0&size=10",
            HttpMethod.GET,
            new HttpEntity<>(customerHeaders),
            new ParameterizedTypeReference<PageResponse<UserResponseDto>>() {});

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void getUserById_Success() {
    // Create a customer using admin token
    ResponseEntity<UserResponseDto> createResponse =
        restTemplate.postForEntity(
            AUTH_URL + "/register",
            new HttpEntity<>(customerRequestDto, adminHeaders),
            UserResponseDto.class);
    String userId = Objects.requireNonNull(createResponse.getBody()).getId();

    // Get user by ID using admin token
    ResponseEntity<UserResponseDto> response =
        restTemplate.exchange(
            USERS_URL + "/" + userId,
            HttpMethod.GET,
            new HttpEntity<>(adminHeaders),
            UserResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(userId, response.getBody().getId());
    assertEquals(customerRequestDto.getEmail(), response.getBody().getEmail());
  }

  @Test
  void updateUser_Success() {
    // Create a customer using admin token
    ResponseEntity<UserResponseDto> createResponse =
        restTemplate.postForEntity(
            AUTH_URL + "/register",
            new HttpEntity<>(customerRequestDto, adminHeaders),
            UserResponseDto.class);
    String userId = Objects.requireNonNull(createResponse.getBody()).getId();

    // Update customer
    customerRequestDto.setFirstName("Updated");
    customerRequestDto.setLastName("Name");

    ResponseEntity<UserResponseDto> response =
        restTemplate.exchange(
            USERS_URL + "/" + userId,
            HttpMethod.PUT,
            new HttpEntity<>(customerRequestDto, adminHeaders),
            UserResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Updated", response.getBody().getFirstName());
    assertEquals("Name", response.getBody().getLastName());
  }

  @Test
  void deleteUser_Success() {
    // Create a customer using admin token
    ResponseEntity<UserResponseDto> createResponse =
        restTemplate.postForEntity(
            AUTH_URL + "/register",
            new HttpEntity<>(customerRequestDto, adminHeaders),
            UserResponseDto.class);
    String userId = Objects.requireNonNull(createResponse.getBody()).getId();

    // Delete user using admin token
    restTemplate.exchange(
        USERS_URL + "/" + userId, HttpMethod.DELETE, new HttpEntity<>(adminHeaders), Void.class);

    // Verify user is deleted
    ResponseEntity<UserResponseDto> getResponse =
        restTemplate.exchange(
            USERS_URL + "/" + userId,
            HttpMethod.GET,
            new HttpEntity<>(adminHeaders),
            UserResponseDto.class);
    assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    assertFalse(userRepository.findById(userId).isPresent());
  }

  @Test
  void getUserById_NotFound() {
    ResponseEntity<UserResponseDto> response =
        restTemplate.exchange(
            USERS_URL + "/nonexistentId",
            HttpMethod.GET,
            new HttpEntity<>(adminHeaders),
            UserResponseDto.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void registerUser_InvalidEmail_Fails() {
    customerRequestDto.setEmail("invalid-email");

    ResponseEntity<UserResponseDto> response =
        restTemplate.postForEntity(
            AUTH_URL + "/register",
            new HttpEntity<>(customerRequestDto, headers),
            UserResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  //  @Test
  //  void partialUpdateUser_Success() {
  //    // Create a customer using admin token
  //    ResponseEntity<UserResponseDto> createResponse = restTemplate.postForEntity(
  //        "/api/users/register",
  //        new HttpEntity<>(customerRequestDto, adminHeaders),
  //        UserResponseDto.class
  //    );
  //    String userId = Objects.requireNonNull(createResponse.getBody()).getId();
  //    String originalEmail = createResponse.getBody().getEmail();
  //
  //    // Create a request with only the fields to update
  //    UserRequestDto partialUpdateRequest = new UserRequestDto();
  //    partialUpdateRequest.setFirstName("Updated");
  //    partialUpdateRequest.setLastName("Name");
  //    // Note: email is not set, should remain unchanged
  //
  //    // Attempt partial update with admin token
  //    ResponseEntity<UserResponseDto> response = restTemplate.exchange(
  //        "/api/users/" + userId,
  //        HttpMethod.PATCH,
  //        new HttpEntity<>(partialUpdateRequest, adminHeaders),
  //        UserResponseDto.class
  //    );
  //
  //    // Verify response
  //    assertEquals(HttpStatus.OK, response.getStatusCode());
  //    assertNotNull(response.getBody());
  //    assertEquals("Updated", response.getBody().getFirstName());
  //    assertEquals("Name", response.getBody().getLastName());
  //    assertEquals(originalEmail, response.getBody().getEmail()); // Email should remain unchanged
  //
  //    // Verify database state
  //    UserEntity updatedUser = userRepository.findById(userId)
  //        .orElseThrow(() -> new RuntimeException("User not found"));
  //    assertEquals("Updated", updatedUser.getFirstName());
  //    assertEquals("Name", updatedUser.getLastName());
  //    assertEquals(originalEmail, updatedUser.getEmail());
  //  }
  //
  //  @Test
  //  void partialUpdateUser_InvalidField_Fails() {
  //    // Create a request with invalid email
  //    UserRequestDto partialUpdateRequest = new UserRequestDto();
  //    partialUpdateRequest.setEmail("invalid-email");
  //
  //    // Get admin ID from repository
  //    String adminId = userRepository.findByEmail(adminRequestDto.getEmail())
  //        .orElseThrow(() -> new RuntimeException("Admin not found"))
  //        .getId();
  //
  //    // Attempt partial update with admin token
  //    ResponseEntity<UserResponseDto> response = restTemplate.exchange(
  //        "/api/users/" + adminId,
  //        HttpMethod.PATCH,
  //        new HttpEntity<>(partialUpdateRequest, adminHeaders),
  //        UserResponseDto.class
  //    );
  //
  //    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  //
  //    // Verify original data remains unchanged
  //    UserEntity unchangedUser = userRepository.findById(adminId)
  //        .orElseThrow(() -> new RuntimeException("User not found"));
  //    assertEquals(adminRequestDto.getEmail(), unchangedUser.getEmail());
  //  }
}
