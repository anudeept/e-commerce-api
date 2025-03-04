package com.ecommerce.users;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {
  boolean existsByEmail(String email);

  Optional<UserEntity> findByEmail(String email);

  boolean existsByRolesContaining(Role role);

  List<UserEntity> findByRolesContaining(Role role);
}
