package com.ecommerce.auth;

import com.ecommerce.security.JwtTokenProvider;
import com.ecommerce.shoppingcart.ShoppingCartEntity;
import com.ecommerce.shoppingcart.ShoppingCartRepository;
import com.ecommerce.users.Role;
import com.ecommerce.users.UserEntity;
import com.ecommerce.users.UserRepository;
import com.ecommerce.users.dto.UserRequestDto;
import com.ecommerce.users.dto.UserResponseDto;

import java.util.Collections;
import java.util.EnumSet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class AuthService {

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private JwtTokenProvider tokenProvider;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private UserRepository userRepository;

  @Autowired private ShoppingCartRepository cartRepository;

  public String authenticateUser(String email, String password) {
    log.debug("Attempting authentication for user: {}", email);

    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password));

    return tokenProvider.generateToken(authentication);
  }
  public UserResponseDto registerUser(UserRequestDto user) {
    // Email validation
    if (userRepository.existsByEmail(user.getEmail())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
    }

    // Role validation
    if (!EnumSet.of(Role.ROLE_CUSTOMER, Role.ROLE_ADMIN, Role.ROLE_STAFF)
        .contains(user.getRole())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
    }

    // Check if admin already exists when registering as admin
    if (user.getRole() == Role.ROLE_ADMIN
        && userRepository.existsByRolesContaining(Role.ROLE_ADMIN)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin already exists");
    }

    // Hash password before saving
    String hashedPassword = passwordEncoder.encode(user.getPassword());
    UserEntity userEntity = new UserEntity(user);
    userEntity.setPassword(hashedPassword);

    UserEntity savedUser = userRepository.save(userEntity);

    try {
      ShoppingCartEntity cart = new ShoppingCartEntity();
      cart.setUserId(savedUser.getId());
      cartRepository.save(cart);
    } catch (Exception e) {
      userRepository.delete(savedUser);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create shopping cart for user");
    }

    return savedUser.toResponseDto();
  }
}
