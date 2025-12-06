package com.example.coditas.mini_e_commerce.controller;

import com.example.coditas.mini_e_commerce.dto.AddToCartRequestDto;
import com.example.coditas.mini_e_commerce.dto.ApiResponseDto;
import com.example.coditas.mini_e_commerce.dto.CartResponseDto;
import com.example.coditas.mini_e_commerce.dto.UpdateCartItemRequestDto;
import com.example.coditas.mini_e_commerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<CartResponseDto>> getCart() {
        CartResponseDto data = cartService.getCurrentUserCart();
        return ResponseEntity.ok(ApiResponseDto.ok(data, "Cart retrieved successfully"));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponseDto<CartResponseDto>> addToCart(
            @Valid @RequestBody AddToCartRequestDto dto) {
        CartResponseDto data = cartService.addToCart(dto);
        return ResponseEntity.ok(ApiResponseDto.ok(data, "Item added to cart"));
    }

    @PatchMapping("/update/{productId}")
    public ResponseEntity<ApiResponseDto<CartResponseDto>> updateQuantity(
            @PathVariable String productId,
            @Valid @RequestBody UpdateCartItemRequestDto dto) {
        CartResponseDto data = cartService.updateQuantity(productId, dto.getQuantity());
        return ResponseEntity.ok(ApiResponseDto.ok(data, "Cart updated"));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponseDto<CartResponseDto>> removeFromCart(
            @PathVariable String productId) {
        CartResponseDto data = cartService.removeFromCart(productId);
        return ResponseEntity.ok(ApiResponseDto.ok(data, "Item removed from cart"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponseDto<String>> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Cart cleared successfully"));
    }
}
