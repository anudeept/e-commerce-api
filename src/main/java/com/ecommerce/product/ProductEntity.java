package com.ecommerce.product;

import com.ecommerce.product.dto.ProductRequestDto;
import com.ecommerce.product.dto.ProductResponseDto;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "products")
public class ProductEntity {
  @Id private String id;
  private String name;
  private String description;
  private double price;
  private int stockQuantity;
  private String category;
  private Boolean active = true;
  private String createdAt = new Date().toString();
  private String updatedAt = new Date().toString();

  public ProductEntity(ProductRequestDto requestDto) {
    this.name = requestDto.getName();
    this.description = requestDto.getDescription();
    this.price = requestDto.getPrice();
    this.stockQuantity = requestDto.getStockQuantity();
    this.category = requestDto.getCategory();
    this.active = requestDto.getActive();
  }

  // toResponseDto
  public ProductResponseDto toResponseDto() {
    ProductResponseDto responseDto = new ProductResponseDto();
    responseDto.setId(id);
    responseDto.setName(name);
    responseDto.setDescription(description);
    responseDto.setPrice(price);
    responseDto.setStockQuantity(stockQuantity);
    responseDto.setCategory(category);
    responseDto.setActive(active);
    responseDto.setCreatedAt(createdAt);
    responseDto.setUpdatedAt(updatedAt);
    return responseDto;
  }
}
