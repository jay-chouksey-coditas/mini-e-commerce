package com.example.coditas.mini_e_commerce.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class GenericFilterDto {
    private String name;
    private String category; // TODO: need to fix
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String status;
}