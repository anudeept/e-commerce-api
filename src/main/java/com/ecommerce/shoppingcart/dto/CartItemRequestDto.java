package com.ecommerce.shoppingcart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequestDto {
  @NotBlank(message = "Product ID is required")
  @Schema(description = "Product ID to add to cart", example = "prod123", required = true)
  private String productId;

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be at least 1")
  @Schema(description = "Quantity of the product", example = "1", minimum = "1", required = true)
  private Integer quantity;
}
