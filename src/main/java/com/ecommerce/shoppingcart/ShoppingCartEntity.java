package com.ecommerce.shoppingcart;

import com.ecommerce.shoppingcart.dto.CartItemRequestDto;
import com.ecommerce.shoppingcart.dto.CartItemResponseDto;
import com.ecommerce.shoppingcart.dto.ShoppingCartResponseDto;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "shopping_carts")
@NoArgsConstructor
public class ShoppingCartEntity {
  @Id private String id;
  private String userId;
  private List<CartItem> items = new ArrayList<>();
  private String createdAt = new Date().toString();

  // Convert to ResponseDto
  public ShoppingCartResponseDto toResponse() {
    ShoppingCartResponseDto responseDto = new ShoppingCartResponseDto();
    responseDto.setId(this.id);
    responseDto.setUserId(this.userId);
    responseDto.setCreatedAt(this.createdAt);

    // Convert cart items
    List<CartItemResponseDto> itemResponses =
        this.items.stream().map(CartItem::toResponse).collect(Collectors.toList());
    responseDto.setItems(itemResponses);

    // Calculate totals
    responseDto.setTotalItems(this.items.stream().mapToInt(CartItem::getQuantity).sum());

    // Note: totalPrice will need to be calculated with product prices from product service
    return responseDto;
  }
}

@Data
@NoArgsConstructor
class CartItem {
  private String productId;
  private int quantity;

  // Constructor from RequestDto
  public CartItem(CartItemRequestDto requestDto) {
    this.productId = requestDto.getProductId();
    this.quantity = requestDto.getQuantity();
  }

  // Convert to ResponseDto
  public CartItemResponseDto toResponse() {
    CartItemResponseDto responseDto = new CartItemResponseDto();
    responseDto.setProductId(this.productId);
    responseDto.setQuantity(this.quantity);
    // Note: product name and prices will need to be set from product service
    return responseDto;
  }
}
