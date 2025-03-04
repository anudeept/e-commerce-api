package com.ecommerce.shoppingcart;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingCartRepository extends MongoRepository<ShoppingCartEntity, String> {
  void deleteByUserId(String userId);
}
