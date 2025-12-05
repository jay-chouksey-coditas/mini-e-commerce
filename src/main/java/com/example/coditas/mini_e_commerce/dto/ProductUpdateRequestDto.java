package com.example.coditas.mini_e_commerce.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateRequestDto {
    private String name;
    private String description;

    @Positive(message = "Price must be positive")
    private BigDecimal price;
    private String category;
    @Positive(message = "Stock must be positive")
    private Integer stock;
}
