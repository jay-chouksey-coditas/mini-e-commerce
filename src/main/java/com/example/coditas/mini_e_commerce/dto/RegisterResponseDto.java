package com.example.coditas.mini_e_commerce.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponseDto {
    String userId;
    String name;
    String email;
}
