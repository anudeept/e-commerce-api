package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Product Response Data Transfer Object")
public class ProductResponseDto {
  private String id;
  private String name;
  private String description;
  private double price;
  private Integer stockQuantity;
  private String category;
  private Boolean active;
  private String createdAt;
  private String updatedAt;
}
