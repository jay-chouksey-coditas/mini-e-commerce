package com.example.coditas.mini_e_commerce.controller;

import com.example.coditas.mini_e_commerce.dto.*;
import com.example.coditas.mini_e_commerce.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> loginUser(@Valid @RequestBody LoginRequestDto payload) {

        LoginResponseDto data = authService.loginUser(payload);

        ApiResponseDto<LoginResponseDto> responseBody = ApiResponseDto.ok(
                data, "User logged in successfully"
        );

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<RegisterResponseDto>> registerUser(@Valid @RequestBody RegisterRequestDto payload){
        RegisterResponseDto data = authService.registerUser(payload);
        ApiResponseDto<RegisterResponseDto> responseBody = ApiResponseDto.ok(
                data, "Success"
        );

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDto<RefreshTokenDto>> getRefreshToken(@RequestBody @Valid RefreshTokenRequestDto payload) {
        RefreshTokenDto data = authService.getRefreshToken(payload);

        ApiResponseDto<RefreshTokenDto> responseBody = ApiResponseDto.ok(
                data, "New Access Token fetched successfully"
        );

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<String>> logoutUser(@RequestBody @Valid RefreshTokenRequestDto payload) {
        String data = authService.logoutUser(payload);

        ApiResponseDto<String> responseBody = ApiResponseDto.ok(
                data, "User logged out successfully"
        );

        return ResponseEntity.ok(responseBody);
    }
}
