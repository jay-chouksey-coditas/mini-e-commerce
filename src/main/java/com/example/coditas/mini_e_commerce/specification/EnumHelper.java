package com.example.coditas.mini_e_commerce.specification;

import com.example.coditas.mini_e_commerce.enums.ActiveStatus;
import com.example.coditas.mini_e_commerce.enums.Category;
import com.example.coditas.mini_e_commerce.enums.OrderStatus;
import com.example.coditas.mini_e_commerce.enums.UserRole;
import org.springframework.util.StringUtils;

public final class EnumHelper {

    private EnumHelper() {
    }

    // Returns the enum constant or null if the string is empty
    public static ActiveStatus toActiveStatus(String value) {
        if (!StringUtils.hasText(value)) return null;
        try {
            return ActiveStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Category toCategory(String value){
        if (!StringUtils.hasText(value)) return null;
        try {
            return Category.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static UserRole toRole(String value){
        if (!StringUtils.hasText(value)) return null;
        try {
            return UserRole.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static OrderStatus toOrderStatus(String value) {
        if (!StringUtils.hasText(value)) return null;
        try {
            return OrderStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
