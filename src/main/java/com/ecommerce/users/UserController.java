package com.ecommerce.users;

import com.ecommerce.common.PageResponse;
import com.ecommerce.users.dto.UserPartialUpdateDto;
import com.ecommerce.users.dto.UserRequestDto;
import com.ecommerce.users.dto.UserResponseDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ecommerce/users")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserController {

  @Autowired private UserService userService;

  @GetMapping("/{id}")
  public ResponseEntity<UserResponseDto> getUserById(@PathVariable String id) {
    return ResponseEntity.ok(userService.getUserById(id).toResponseDto());
  }

  @GetMapping
  public ResponseEntity<PageResponse<UserResponseDto>> getAllUsers(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    Page<UserResponseDto> userPage = userService.getAllUsers(PageRequest.of(page, size));

    PageResponse<UserResponseDto> response = new PageResponse<>();
    response.setContent(userPage.getContent());
    response.setPageNumber(userPage.getNumber());
    response.setPageSize(userPage.getSize());
    response.setTotalElements(userPage.getTotalElements());
    response.setTotalPages(userPage.getTotalPages());
    response.setLast(userPage.isLast());
    response.setFirst(userPage.isFirst());

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable String id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserResponseDto> updateUser(
      @PathVariable String id, @RequestBody @Valid UserRequestDto userDetails) {
    return ResponseEntity.ok(userService.updateUser(id, userDetails));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<UserResponseDto> partialUpdateUser(
      @PathVariable String id, @Valid @RequestBody UserPartialUpdateDto updateDto) {
    return ResponseEntity.ok(userService.partialUpdateUser(id, updateDto));
  }
}
