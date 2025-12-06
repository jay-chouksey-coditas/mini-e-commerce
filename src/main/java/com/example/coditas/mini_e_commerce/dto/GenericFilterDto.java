package com.example.coditas.mini_e_commerce.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class GenericFilterDto {
    private String name;
    private String category; // TODO: need to fix
    private String role;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}