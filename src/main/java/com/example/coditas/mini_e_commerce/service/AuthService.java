package com.example.coditas.mini_e_commerce.service;

import com.example.coditas.mini_e_commerce.dto.*;
import com.example.coditas.mini_e_commerce.entity.RefreshToken;
import com.example.coditas.mini_e_commerce.entity.User;
import com.example.coditas.mini_e_commerce.enums.ActiveStatus;
import com.example.coditas.mini_e_commerce.enums.UserRole;
import com.example.coditas.mini_e_commerce.exception.CustomException;
import com.example.coditas.mini_e_commerce.repository.RefreshTokenRepository;
import com.example.coditas.mini_e_commerce.repository.UserRepository;
import com.example.coditas.mini_e_commerce.util.JwtService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, AuthenticationManager authManager, JwtService jwtService, RefreshTokenService refreshTokenService, RefreshTokenRepository refreshTokenRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponseDto loginUser(LoginRequestDto request) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Fetch the user safely
            User savedUser = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

            if (!authentication.isAuthenticated()) {
                throw new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED);
            }

            // Generate JWT
            String jwtToken = jwtService.generateToken(request.getEmail());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());

            log.info("User {} logged in successfully", savedUser.getName());

            // Build response DTO
            return LoginResponseDto.builder()
                    .userId(savedUser.getId())
                    .email(savedUser.getEmail())
                    .name(savedUser.getName())
                    .roleName(String.valueOf(savedUser.getRole()))
                    .refreshToken(refreshToken.getToken())
                    .accessToken(jwtToken)
                    .build();

        } catch (BadCredentialsException ex) {
            throw new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        } catch (CustomException ex) {
            throw ex; // GlobalExceptionHandler will handle it
        } catch (Exception ex) {
            throw new CustomException(
                    ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

 @Transactional
    public RegisterResponseDto registerUser(RegisterRequestDto dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException("Email already exists: " + dto.getEmail(), HttpStatus.CONFLICT);
        }

        // Generate unique userId
        String userId = String.valueOf(UUID.randomUUID());

        // Build User entity
        User customer = User.builder()
                .userId(userId)
                .name(dto.getName().trim())
                .email(dto.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(UserRole.CUSTOMER)
                .isActive(ActiveStatus.ACTIVE)
                .build();

         customer = userRepository.save(customer);

        log.info("Distributor created successfully: {} ({})", customer.getName(), customer.getUserId());

        return RegisterResponseDto.builder()
                .userId(customer.getUserId())
                .name(customer.getName())
                .email(customer.getEmail())
                .build();
    }

    public RefreshTokenDto getRefreshToken(RefreshTokenRequestDto refreshTokenRequestDto){
        String requestToken = refreshTokenRequestDto.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new CustomException(
                        "Invalid Refresh Token", HttpStatus.BAD_REQUEST
                ));

        if(refreshTokenService.isTokenExpired(refreshToken)){
            throw new CustomException(
                    "Refresh token expired. Please login again.", HttpStatus.BAD_REQUEST
            );
        }

        String newAccessToken = jwtService.generateToken(refreshToken.getUser().getEmail());

        RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
        refreshTokenDto.setAccessToken(newAccessToken);

        log.info("Refresh token generated successfully");

        return refreshTokenDto;
    }

    public String logoutUser(RefreshTokenRequestDto refreshTokenRequestDto) {
        String requestToken = refreshTokenRequestDto.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow( () -> new CustomException("Invalid refresh token.", HttpStatus.BAD_REQUEST));

        refreshTokenRepository.delete(refreshToken);

        log.info("User logged out successfully");

        return "Logged out successfully.";
    }
}
