package com.ecommerce.product;

import com.ecommerce.common.PageResponse;
import com.ecommerce.product.dto.ProductPartialUpdateDto;
import com.ecommerce.product.dto.ProductRequestDto;
import com.ecommerce.product.dto.ProductResponseDto;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ecommerce/products")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')")
public class ProductController {

  @Autowired private ProductService productService;

  @PostMapping
  public ResponseEntity<ProductResponseDto> createProduct(
      @RequestBody @Valid ProductRequestDto requestDto) {
    return new ResponseEntity<>(productService.createProduct(requestDto), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductEntity> getProductById(@PathVariable String id) {
    return ResponseEntity.ok(productService.getProductById(id));
  }

  @GetMapping
  public ResponseEntity<PageResponse<ProductResponseDto>> getAllProducts(
      @RequestParam(required = false) String category,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(required = false) String searchTerm,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "name") String sortBy,
      @RequestParam(defaultValue = "asc") String sortDirection) {

    Sort.Direction direction =
        sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
    Page<ProductResponseDto> products =
        productService.getAllProducts(category, minPrice, maxPrice, searchTerm, pageRequest);
    PageResponse<ProductResponseDto> pageResponse = new PageResponse<>();
    pageResponse.setContent(products.getContent());
    pageResponse.setPageNumber(products.getNumber());
    pageResponse.setPageSize(products.getSize());
    pageResponse.setTotalElements(products.getTotalElements());
    pageResponse.setTotalPages(products.getTotalPages());
    pageResponse.setLast(products.isLast());
    pageResponse.setFirst(products.isFirst());
    return ResponseEntity.ok(pageResponse);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
    productService.deleteProduct(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductResponseDto> updateProduct(
      @PathVariable String id, @RequestBody @Valid ProductRequestDto requestDto) {
    return ResponseEntity.ok(productService.updateProduct(id, requestDto));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ProductResponseDto> partialUpdateProduct(
      @PathVariable String id, @Valid @RequestBody ProductPartialUpdateDto requestDto) {
    return ResponseEntity.ok(productService.partialUpdateProduct(id, requestDto));
  }
}
