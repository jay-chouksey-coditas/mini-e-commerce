package com.example.coditas.mini_e_commerce.enums;

import com.example.coditas.mini_e_commerce.exception.CustomException;
import org.springframework.http.HttpStatus;

public enum OrderStatus {
    PENDING, ACCEPTED, REJECTED;

    public static OrderStatus getStatus(String val){
        return switch (val){
            case "PENDING" -> PENDING;
            case "ACCEPTED" -> ACCEPTED;
            case "REJECTED" -> REJECTED;
            default -> throw new CustomException("Please enter valid status.", HttpStatus.BAD_REQUEST);
        };
    }
}
