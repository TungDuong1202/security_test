package com.example.user.controller;

import com.example.user.dto.request.CreateUserRequest;
import com.example.user.dto.request.LoginRequest;
import com.example.user.dto.response.ApiResponseEntity;
import com.example.user.dto.response.ApiResponseFactory;
import com.example.user.dto.response.AuthResponse;
import com.example.user.dto.response.UserResponse;
import com.example.user.service.IAuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Auth Management", description = "APIs for managing authentication (Register, Login)")
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/register")
    public ApiResponseEntity<UserResponse> register(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return ApiResponseFactory.created(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ApiResponseFactory.success(authService.login(request));
    }
}
