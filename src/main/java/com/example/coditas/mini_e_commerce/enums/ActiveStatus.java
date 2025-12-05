package com.example.coditas.mini_e_commerce.enums;

import com.example.coditas.mini_e_commerce.exception.CustomException;
import org.springframework.http.HttpStatus;

public enum ActiveStatus {
    ACTIVE(), INACTIVE();

    public static ActiveStatus getType(String val) {
        return switch (val) {
            case "ACTIVE" -> ACTIVE;
            case "INACTIVE" -> INACTIVE;
            default -> throw new CustomException("Please enter a valid 'Active Status' type.", HttpStatus.BAD_REQUEST);
        };
    }
}
