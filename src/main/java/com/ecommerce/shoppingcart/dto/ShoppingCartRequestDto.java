package com.ecommerce.shoppingcart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class ShoppingCartRequestDto {
  @NotBlank(message = "User ID is required")
  @Schema(description = "User ID who owns the cart", example = "user123", required = true)
  private String userId;

  @NotNull(message = "Items list cannot be null")
  @Valid
  @Schema(description = "List of items in the cart")
  private List<CartItemRequestDto> items;
}
