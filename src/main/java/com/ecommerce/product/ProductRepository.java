package com.ecommerce.product;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<ProductEntity, String> {
  Page<ProductEntity> findByCategory(String category, Pageable pageable);

  Page<ProductEntity> findByPriceBetween(
      BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

  Page<ProductEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

  Page<ProductEntity> findByActive(Boolean active, Pageable pageable);
}
