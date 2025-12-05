package com.example.coditas.mini_e_commerce.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    String userId;
    String name;
    String email;
    String role;
    String isActive;
}
