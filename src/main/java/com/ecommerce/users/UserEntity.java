package com.ecommerce.users;

import com.ecommerce.users.dto.UserRequestDto;
import com.ecommerce.users.dto.UserResponseDto;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class UserEntity {
  @Id private String id;
  private String firstName;
  private String lastName;
  private String email;
  private String password;
  private Set<Role> roles = new HashSet<>();
  private String createdAt = new Date().toString();

  public UserEntity(UserRequestDto user) {
    this.firstName = user.getFirstName();
    this.lastName = user.getLastName();
    this.email = user.getEmail();
    this.password = user.getPassword();
    this.roles.add(user.getRole());
  }

  // to user response dto
  public UserResponseDto toResponseDto() {
    UserResponseDto responseDto = new UserResponseDto();
    responseDto.setId(this.id);
    responseDto.setFirstName(this.firstName);
    responseDto.setLastName(this.lastName);
    responseDto.setEmail(this.email);
    responseDto.setRoles(this.roles.stream().map(Role::name).collect(Collectors.toSet()));
    responseDto.setCreatedAt(this.createdAt);
    return responseDto;
  }

  public Set<SimpleGrantedAuthority> getAuthorities() {
    Set<SimpleGrantedAuthority> authorities = new HashSet<>();
    roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role.name())));
    return authorities;
  }
}
