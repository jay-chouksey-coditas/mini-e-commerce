package com.example.coditas.mini_e_commerce.enums;

import com.example.coditas.mini_e_commerce.exception.CustomException;
import org.springframework.http.HttpStatus;

public enum UserRole {
    ADMIN, VENDOR, CUSTOMER;

    public static UserRole getRole(String val){
        return switch (val){
            case "ADMIN" -> ADMIN;
            case "VENDOR" -> VENDOR;
            case "CUSTOMER" -> CUSTOMER;
            default -> throw new CustomException("Please enter valid role.", HttpStatus.BAD_REQUEST);
        };
    }
}
