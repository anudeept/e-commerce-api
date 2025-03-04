package com.ecommerce.shoppingcart;

import com.ecommerce.product.ProductEntity;
import com.ecommerce.product.ProductRepository;
import com.ecommerce.shoppingcart.dto.CartItemRequestDto;
import com.ecommerce.shoppingcart.dto.CartItemResponseDto;
import com.ecommerce.shoppingcart.dto.ShoppingCartResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ShoppingCartService {

  @Autowired private ShoppingCartRepository cartRepository;

  @Autowired private ProductRepository productRepository;

  public ShoppingCartResponseDto createCart(String userId) {
    ShoppingCartEntity cart = new ShoppingCartEntity();
    cart.setUserId(userId);
    ShoppingCartEntity savedCart = cartRepository.save(cart);
    return enrichCartResponse(savedCart);
  }

  public ShoppingCartResponseDto getCartById(String cartId) {
    ShoppingCartEntity cart = findCartById(cartId);
    return enrichCartResponse(cart);
  }

  public ShoppingCartResponseDto addItemToCart(String cartId, CartItemRequestDto itemRequest) {
    ShoppingCartEntity cart = findCartById(cartId);
    verifyAndGetProduct(itemRequest.getProductId(), itemRequest.getQuantity());

    boolean itemExists = false;
    for (CartItem item : cart.getItems()) {
      if (item.getProductId().equals(itemRequest.getProductId())) {
        verifyAndGetProduct(
            itemRequest.getProductId(), item.getQuantity() + itemRequest.getQuantity());
        item.setQuantity(item.getQuantity() + itemRequest.getQuantity());
        itemExists = true;
        break;
      }
    }

    if (!itemExists) {
      cart.getItems().add(new CartItem(itemRequest));
    }

    ShoppingCartEntity savedCart = cartRepository.save(cart);
    return enrichCartResponse(savedCart);
  }

  public ShoppingCartResponseDto removeItemFromCart(String cartId, String productId) {
    ShoppingCartEntity cart = findCartById(cartId);
    cart.getItems().removeIf(item -> item.getProductId().equals(productId));
    ShoppingCartEntity savedCart = cartRepository.save(cart);
    return enrichCartResponse(savedCart);
  }

  public ShoppingCartResponseDto updateItemQuantity(String cartId, CartItemRequestDto itemRequest) {
    ShoppingCartEntity cart = findCartById(cartId);
    verifyAndGetProduct(itemRequest.getProductId(), itemRequest.getQuantity());

    boolean itemFound = false;
    for (CartItem item : cart.getItems()) {
      if (item.getProductId().equals(itemRequest.getProductId())) {
        item.setQuantity(itemRequest.getQuantity());
        itemFound = true;
        break;
      }
    }

    if (!itemFound) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in cart");
    }

    ShoppingCartEntity savedCart = cartRepository.save(cart);
    return enrichCartResponse(savedCart);
  }

  public ShoppingCartResponseDto clearCart(String cartId) {
    ShoppingCartEntity cart = findCartById(cartId);
    cart.getItems().clear();
    ShoppingCartEntity savedCart = cartRepository.save(cart);
    return enrichCartResponse(savedCart);
  }

  public void deleteCart(String cartId) {
    if (!cartRepository.existsById(cartId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping cart not found");
    }
    cartRepository.deleteById(cartId);
  }

  // Helper methods
  private ShoppingCartEntity findCartById(String cartId) {
    return cartRepository
        .findById(cartId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping cart not found"));
  }

  private ProductEntity verifyAndGetProduct(String productId, int quantity) {
    ProductEntity product =
        productRepository
            .findById(productId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

    if (product.getStockQuantity() < quantity) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock quantity");
    }

    return product;
  }

  private ShoppingCartResponseDto enrichCartResponse(ShoppingCartEntity cart) {
    ShoppingCartResponseDto responseDto = cart.toResponse();
    double totalPrice = 0.0;

    for (CartItemResponseDto itemDto : responseDto.getItems()) {
      ProductEntity product =
          productRepository
              .findById(itemDto.getProductId())
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

      itemDto.setProductName(product.getName());
      itemDto.setProductPrice(product.getPrice());
      itemDto.setTotalPrice(product.getPrice() * itemDto.getQuantity());

      totalPrice += itemDto.getTotalPrice();
    }

    responseDto.setTotalPrice(totalPrice);
    return responseDto;
  }
}
