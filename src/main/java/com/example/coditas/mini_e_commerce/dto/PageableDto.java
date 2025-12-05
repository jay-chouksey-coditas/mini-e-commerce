package com.example.coditas.mini_e_commerce.dto;

import lombok.Data;

@Data
public class PageableDto {
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDir = "desc";
}
