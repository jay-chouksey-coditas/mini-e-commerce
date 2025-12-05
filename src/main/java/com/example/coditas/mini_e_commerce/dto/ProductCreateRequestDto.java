package com.example.coditas.mini_e_commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductCreateRequestDto {

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Unit price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal unitPrice;

    @NotNull(message = "Category is required")
    private String category;

    @NotNull(message = "Stock is required")
    @Positive(message = "Price must be positive")
    private Integer stock;
}
