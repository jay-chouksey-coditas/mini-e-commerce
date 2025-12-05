package com.example.coditas.mini_e_commerce.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class ProductResponseDto {
    private String productId;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal unitPrice;
    private String categoryName;
    private String status;
    private String createdOn;
    private Integer currentStock;
}