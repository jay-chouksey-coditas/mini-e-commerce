package com.example.coditas.mini_e_commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    String orderId;
    String status;
    BigDecimal totalPrice;
    Integer totalQuantity;
    String createdAt;
    List<OrderItemResponseDto> items;
}
