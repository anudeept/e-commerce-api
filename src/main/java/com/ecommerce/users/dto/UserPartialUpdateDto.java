package com.ecommerce.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserPartialUpdateDto {
  @Size(min = 2, max = 25, message = "First name must be between 2 and 25 characters")
  @Pattern(regexp = "^[a-zA-Z]*$", message = "First name can only contain letters")
  @Schema(description = "User's first name", example = "John")
  private String firstName;

  @Size(min = 2, max = 25, message = "Last name must be between 2 and 25 characters")
  @Pattern(regexp = "^[a-zA-Z]*$", message = "Last name can only contain letters")
  @Schema(description = "User's last name", example = "Doe")
  private String lastName;

  @Email(message = "Please provide a valid email address")
  @Size(max = 100, message = "Email must not exceed 100 characters")
  @Schema(description = "User's email address", example = "john.doe@example.com")
  private String email;
}
