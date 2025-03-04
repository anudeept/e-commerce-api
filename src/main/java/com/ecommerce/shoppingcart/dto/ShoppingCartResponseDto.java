package com.ecommerce.shoppingcart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
public class ShoppingCartResponseDto {
  @Schema(description = "Shopping cart ID", example = "cart123")
  private String id;

  @Schema(description = "User ID who owns the cart", example = "user123")
  private String userId;

  @Schema(description = "List of items in the cart")
  private List<CartItemResponseDto> items;

  @Schema(description = "Cart creation timestamp", example = "2024-03-20T10:30:00Z")
  private String createdAt;

  @Schema(description = "Total number of items in cart", example = "5")
  private int totalItems;

  @Schema(description = "Total price of all items in cart", example = "149.95")
  private Double totalPrice;
}
