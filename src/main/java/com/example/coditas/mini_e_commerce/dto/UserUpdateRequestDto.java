package com.example.coditas.mini_e_commerce.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequestDto {
    String name;
    String email;
    String password;
    String role;
}
