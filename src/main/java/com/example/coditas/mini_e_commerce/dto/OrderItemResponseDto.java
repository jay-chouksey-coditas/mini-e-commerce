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
public class OrderItemResponseDto {
    String productId;
    String productName;
    BigDecimal priceAtOrder;
    Integer quantity;
    BigDecimal subtotal;
}
