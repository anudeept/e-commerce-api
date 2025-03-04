package com.ecommerce.users;

import com.ecommerce.shoppingcart.ShoppingCartEntity;
import com.ecommerce.shoppingcart.ShoppingCartRepository;
import com.ecommerce.users.dto.UserPartialUpdateDto;
import com.ecommerce.users.dto.UserRequestDto;
import com.ecommerce.users.dto.UserResponseDto;
import java.util.EnumSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

  @Autowired private UserRepository userRepository;

  @Autowired private ShoppingCartRepository cartRepository;

  @Autowired private PasswordEncoder passwordEncoder;

 

  public UserEntity getUserById(String id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  public Page<UserResponseDto> getAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable).map(UserEntity::toResponseDto);
  }

  public void deleteUser(String id) {
    UserEntity user = getUserById(id);

    // Delete associated shopping cart first
    try {
      cartRepository.deleteByUserId(id);
    } catch (Exception e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete user's shopping cart");
    }

    // Then delete the user
    userRepository.delete(user);
  }

  public UserResponseDto updateUser(String id, UserRequestDto userDetails) {
    UserEntity existingUser = getUserById(id);

    // Check if new email conflicts with another user
    if (userDetails.getEmail() != null
        && !userDetails.getEmail().equals(existingUser.getEmail())
        && userRepository.existsByEmail(userDetails.getEmail())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
    }

    // Update only non-null fields
    if (userDetails.getFirstName() != null) {
      existingUser.setFirstName(userDetails.getFirstName());
    }
    if (userDetails.getLastName() != null) {
      existingUser.setLastName(userDetails.getLastName());
    }
    if (userDetails.getEmail() != null) {
      existingUser.setEmail(userDetails.getEmail());
    }
    if (userDetails.getPassword() != null) {
      existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
    }

    return userRepository.save(existingUser).toResponseDto();
  }

  public UserResponseDto partialUpdateUser(String id, UserPartialUpdateDto updateDto) {
    UserEntity existingUser = getUserById(id);

    // Update only non-null fields
    if (updateDto.getFirstName() != null) {
      existingUser.setFirstName(updateDto.getFirstName());
    }
    if (updateDto.getLastName() != null) {
      existingUser.setLastName(updateDto.getLastName());
    }
    if (updateDto.getEmail() != null) {
      // Check if email is already taken by another user
      if (userRepository.existsByEmail(updateDto.getEmail())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
      }
      existingUser.setEmail(updateDto.getEmail());
    }

    UserEntity updatedUser = userRepository.save(existingUser);
    return updatedUser.toResponseDto();
  }
}
