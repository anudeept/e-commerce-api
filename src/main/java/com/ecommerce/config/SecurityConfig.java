package com.ecommerce.config;

import com.ecommerce.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf()
        .disable()
        .authorizeHttpRequests(
            auth ->
                auth
                    // Public endpoints - no authentication needed
                    .requestMatchers(
                        "/api/v1/ecommerce/auth/**", // Allow all auth endpoints including register
                        "/api/v1/ecommerce/auth/login", // Explicitly allow login
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html")
                    .permitAll()

                    // Product management endpoints
                    .requestMatchers(HttpMethod.POST, "/api/v1/ecommerce/products/**")
                    .hasAnyRole("ADMIN", "STAFF")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/ecommerce/products/**")
                    .hasAnyRole("ADMIN", "STAFF")
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/ecommerce/products/**")
                    .hasAnyRole("ADMIN", "STAFF")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/ecommerce/products/**")
                    .hasAnyRole("ADMIN", "STAFF")

                    // Shopping cart endpoints
                    .requestMatchers("/api/v1/ecommerce/carts/**")
                    .hasAnyRole("CUSTOMER", "ADMIN")

                    // User management endpoints
                    .requestMatchers(HttpMethod.GET, "/api/v1/ecommerce/users/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/ecommerce/users/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/ecommerce/users/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/ecommerce/users/**")
                    .hasAnyRole("ADMIN", "CUSTOMER") // Added PATCH
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/ecommerce/users/**")
                    .hasRole("ADMIN")

                    // Product viewing endpoints
                    .requestMatchers(HttpMethod.GET, "/api/v1/ecommerce/products/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}
