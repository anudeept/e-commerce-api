package com.ecommerce.users.dto;

import java.util.Set;
import lombok.Data;

@Data
public class UserResponseDto {
  private String id;
  private String firstName;
  private String lastName;
  private String email;
  private String createdAt;
  private Set<String> roles;
}
