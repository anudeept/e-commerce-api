package com.ecommerce.product;

import com.ecommerce.product.dto.ProductPartialUpdateDto;
import com.ecommerce.product.dto.ProductRequestDto;
import com.ecommerce.product.dto.ProductResponseDto;
import java.math.BigDecimal;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {

  @Autowired private ProductRepository productRepository;

  public ProductResponseDto createProduct(ProductRequestDto requestDto) {
    ProductEntity product = new ProductEntity(requestDto);
    return productRepository.save(product).toResponseDto();
  }

  public ProductEntity getProductById(String id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
  }

  public Page<ProductResponseDto> getAllProducts(
      String category,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      String searchTerm,
      Pageable pageable) {

    if (category != null) {
      return productRepository.findByCategory(category, pageable).map(ProductEntity::toResponseDto);
    }

    if (minPrice != null && maxPrice != null) {
      return productRepository
          .findByPriceBetween(minPrice, maxPrice, pageable)
          .map(ProductEntity::toResponseDto);
    }

    if (searchTerm != null) {
      return productRepository
          .findByNameContainingIgnoreCase(searchTerm, pageable)
          .map(ProductEntity::toResponseDto);
    }

    return productRepository.findAll(pageable).map(ProductEntity::toResponseDto);
  }

  public void deleteProduct(String id) {
    if (!productRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
    }
    productRepository.deleteById(id);
  }

  public ProductResponseDto updateProduct(String id, ProductRequestDto requestDto) {
    ProductEntity product = getProductById(id);

    product.setName(requestDto.getName());
    product.setDescription(requestDto.getDescription());
    product.setPrice(requestDto.getPrice());
    product.setStockQuantity(requestDto.getStockQuantity());
    product.setCategory(requestDto.getCategory());
    product.setActive(requestDto.getActive());
    product.setUpdatedAt(new Date().toString());

    return productRepository.save(product).toResponseDto();
  }

  public ProductResponseDto partialUpdateProduct(String id, ProductPartialUpdateDto requestDto) {
    ProductEntity product = getProductById(id);

    if (requestDto.getName() != null) {
      product.setName(requestDto.getName());
    }
    if (requestDto.getDescription() != null) {
      product.setDescription(requestDto.getDescription());
    }
    if (requestDto.getPrice() != 0) {
      product.setPrice(requestDto.getPrice());
    }
    if (requestDto.getStockQuantity() != 0) {
      product.setStockQuantity(requestDto.getStockQuantity());
    }
    if (requestDto.getCategory() != null) {
      product.setCategory(requestDto.getCategory());
    }
    if (requestDto.getActive() != null) {
      product.setActive(requestDto.getActive());
    }

    product.setUpdatedAt(new Date().toString());
    return productRepository.save(product).toResponseDto();
  }
}
