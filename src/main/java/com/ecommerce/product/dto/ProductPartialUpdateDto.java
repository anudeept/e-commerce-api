package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Product Partial Update Data Transfer Object")
public class ProductPartialUpdateDto {

  @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
  @Schema(description = "Name of the product", example = "Wireless Mouse")
  private String name;

  @Size(max = 500, message = "Description must not exceed 500 characters")
  @Schema(
      description = "Detailed description of the product",
      example = "High-performance wireless mouse with ergonomic design")
  private String description;

  @DecimalMin(value = "0.01", message = "Price must be greater than 0")
  @Digits(integer = 6, fraction = 2, message = "Price must have at most 2 decimal places")
  @Schema(description = "Product price in dollars", example = "29.99")
  private double price;

  @Min(value = 0, message = "Stock quantity cannot be negative")
  @Schema(description = "Available quantity in stock", example = "100", minimum = "0")
  private int stockQuantity;

  @Pattern(
      regexp = "^(Electronics|Clothing|Books|Home|Beauty|Sports|Food|Other)$",
      message =
          "Invalid category. Allowed values: Electronics, Clothing, Books, Home, Beauty, Sports, Food, Other")
  @Schema(
      description = "Product category",
      example = "Electronics",
      allowableValues = {
        "Electronics",
        "Clothing",
        "Books",
        "Home",
        "Beauty",
        "Sports",
        "Food",
        "Other"
      })
  private String category;

  @Schema(description = "Product availability status", example = "true")
  private Boolean active;
}
