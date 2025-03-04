package com.ecommerce.shoppingcart;

import com.ecommerce.shoppingcart.dto.CartItemRequestDto;
import com.ecommerce.shoppingcart.dto.ShoppingCartResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ecommerce/shopping-carts")
@PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
public class ShoppingCartController {

  @Autowired private ShoppingCartService cartService;

  @PostMapping
  public ResponseEntity<ShoppingCartResponseDto> createCart(@RequestParam String userId) {
    return new ResponseEntity<>(cartService.createCart(userId), HttpStatus.CREATED);
  }

  @GetMapping("/{cartId}")
  public ResponseEntity<ShoppingCartResponseDto> getCart(@PathVariable String cartId) {
    return ResponseEntity.ok(cartService.getCartById(cartId));
  }

  @PostMapping("/{cartId}/items")
  public ResponseEntity<ShoppingCartResponseDto> addItemToCart(
      @PathVariable String cartId, @RequestBody CartItemRequestDto itemRequest) {
    return ResponseEntity.ok(cartService.addItemToCart(cartId, itemRequest));
  }

  @DeleteMapping("/{cartId}/items/{productId}")
  public ResponseEntity<ShoppingCartResponseDto> removeItemFromCart(
      @PathVariable String cartId, @PathVariable String productId) {
    return ResponseEntity.ok(cartService.removeItemFromCart(cartId, productId));
  }

  @PutMapping("/{cartId}/items")
  public ResponseEntity<ShoppingCartResponseDto> updateItemQuantity(
      @PathVariable String cartId, @RequestBody CartItemRequestDto itemRequest) {
    return ResponseEntity.ok(cartService.updateItemQuantity(cartId, itemRequest));
  }

  @DeleteMapping("/{cartId}/items")
  public ResponseEntity<ShoppingCartResponseDto> clearCart(@PathVariable String cartId) {
    return ResponseEntity.ok(cartService.clearCart(cartId));
  }

  @DeleteMapping("/{cartId}")
  public ResponseEntity<Void> deleteCart(@PathVariable String cartId) {
    cartService.deleteCart(cartId);
    return ResponseEntity.noContent().build();
  }
}
