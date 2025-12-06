package com.example.coditas.mini_e_commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponseDto {
    String productId;
    String productName;
    String imageUrl;
    BigDecimal unitPrice;
    Integer quantity;
    BigDecimal subtotal;
}
