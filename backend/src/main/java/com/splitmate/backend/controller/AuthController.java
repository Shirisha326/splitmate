package com.splitmate.backend.controller;

import com.splitmate.backend.dto.request.AuthRequest;
import com.splitmate.backend.dto.response.ApiResponse;
import com.splitmate.backend.service.impl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse.Success<ApiResponse.AuthResponse>> register(
            @Valid @RequestBody AuthRequest.Register request) {
        ApiResponse.AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.Success.of("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse.Success<ApiResponse.AuthResponse>> login(
            @Valid @RequestBody AuthRequest.Login request) {
        ApiResponse.AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.Success.of("Login successful", response));
    }
}