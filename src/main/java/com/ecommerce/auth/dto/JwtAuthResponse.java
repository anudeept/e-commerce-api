package com.ecommerce.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JwtAuthResponse {
  private String accessToken;
  private String tokenType = "Bearer";

  public JwtAuthResponse(String accessToken) {
    this.accessToken = accessToken;
  }
}
