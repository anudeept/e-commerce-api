package com.ecommerce.auth;

import com.ecommerce.auth.dto.JwtAuthResponse;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.MessageResponse;
import com.ecommerce.security.JwtTokenProvider;
import com.ecommerce.users.UserEntity;
import com.ecommerce.users.dto.UserRequestDto;
import com.ecommerce.users.dto.UserResponseDto;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/ecommerce/auth")
public class AuthController {

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private JwtTokenProvider tokenProvider;

  @Autowired private AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    try {
      String jwt =
          authService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
      return ResponseEntity.ok(new JwtAuthResponse(jwt));
    } catch (Exception e) {
      log.error("Authentication failed", e);
      return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
    }
  }

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequestDto request) {
    try {
      UserResponseDto user = authService.registerUser(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(user);
    } catch (Exception e) {
      log.error("Registration failed", e);
      return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
    }
  }
}
