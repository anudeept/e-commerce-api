package com.ecommerce.shoppingcart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CartItemResponseDto {
  @Schema(description = "Product ID in cart", example = "prod123")
  private String productId;

  @Schema(description = "Quantity of the product", example = "1")
  private int quantity;

  @Schema(description = "Product name", example = "Wireless Mouse")
  private String productName;

  @Schema(description = "Product price", example = "29.99")
  private Double productPrice;

  @Schema(description = "Total price for this item", example = "59.98")
  private Double totalPrice;
}
