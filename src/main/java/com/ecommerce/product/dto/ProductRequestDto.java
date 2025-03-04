package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Product Request Data Transfer Object")
public class ProductRequestDto {

  @NotBlank(message = "Product name is required")
  @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
  @Schema(description = "Name of the product", example = "iPhone 13 Pro", required = true)
  private String name;

  @Size(max = 1000, message = "Description must not exceed 1000 characters")
  @Schema(
      description = "Detailed description of the product",
      example = "The latest iPhone with pro camera system and A15 Bionic chip")
  private String description;

  @NotNull(message = "Price is required")
  @Positive(message = "Price must be greater than 0")
  @Digits(integer = 10, fraction = 2, message = "Price must have at most 2 decimal places")
  @Schema(description = "Product price in dollars", example = "999.99", required = true)
  private double price;

  @NotNull(message = "Stock quantity is required")
  @Min(value = 0, message = "Stock quantity cannot be negative")
  @Schema(
      description = "Available quantity in stock",
      example = "100",
      minimum = "0",
      required = true)
  private int stockQuantity;

  @NotBlank(message = "Category is required")
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
      },
      required = true)
  private String category;

  @Schema(description = "Product availability status", example = "true", defaultValue = "true")
  private Boolean active = true;
}
