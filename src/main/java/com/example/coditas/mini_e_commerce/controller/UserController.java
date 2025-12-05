package com.example.coditas.mini_e_commerce.controller;

import com.example.coditas.mini_e_commerce.dto.*;
import com.example.coditas.mini_e_commerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    UserService userService;

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserUpdateRequestDto dto) {

        UserResponseDto data = userService.updateUser(userId, dto);
        ApiResponseDto<UserResponseDto> responseBody = ApiResponseDto.ok(data, "User updated successfully");

        return ResponseEntity.ok(responseBody);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<String>> softDeleteUser(
            @PathVariable String userId) {  // assuming you have auth

        String data = userService.softDeleteUser(userId);
        ApiResponseDto<String> responseBody = ApiResponseDto.ok(data, "Success");

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<UserResponseDto>>> getEmployees(
            @ModelAttribute GenericFilterDto filter,
            @ModelAttribute PageableDto page) {

        Page<UserResponseDto> data = userService.searchUsers(filter, page);
        return ResponseEntity.ok(ApiResponseDto.paged(
                data, page.getPage(), page.getSize(), data.getTotalElements()
        ));
    }
}
