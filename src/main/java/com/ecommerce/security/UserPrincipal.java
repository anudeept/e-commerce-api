package com.ecommerce.security;

import com.ecommerce.users.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
  private String id;
  private String email;

  @JsonIgnore private String password;
  private Collection<? extends GrantedAuthority> authorities;

  public static UserPrincipal create(UserEntity user) {
    return new UserPrincipal(
        user.getId(), user.getEmail(), user.getPassword(), user.getAuthorities());
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
