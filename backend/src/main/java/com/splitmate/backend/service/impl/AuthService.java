package com.splitmate.backend.service.impl;

import com.splitmate.backend.dto.request.AuthRequest;
import com.splitmate.backend.dto.response.ApiResponse;
import com.splitmate.backend.entity.User;
import com.splitmate.backend.exception.BadRequestException;
import com.splitmate.backend.exception.DuplicateResourceException;
import com.splitmate.backend.repository.UserRepository;
import com.splitmate.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public ApiResponse.AuthResponse register(AuthRequest.Register request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        String[] colors = {"#6366f1", "#ec4899", "#f59e0b", "#10b981", "#3b82f6", "#8b5cf6", "#ef4444", "#14b8a6"};
        String color = colors[Math.abs(request.getName().charAt(0)) % colors.length];

        User user = User.builder()
            .name(request.getName().trim())
            .email(request.getEmail().toLowerCase().trim())
            .password(passwordEncoder.encode(request.getPassword()))
            .avatarColor(color)
            .build();

        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        return ApiResponse.AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .user(mapToUserResponse(user))
            .build();
    }

    public ApiResponse.AuthResponse login(AuthRequest.Login request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail().toLowerCase(),
                request.getPassword()
            )
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
            .orElseThrow(() -> new BadRequestException("User not found"));

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        return ApiResponse.AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .user(mapToUserResponse(user))
            .build();
    }

    private ApiResponse.UserResponse mapToUserResponse(User user) {
        return ApiResponse.UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .avatarColor(user.getAvatarColor())
            .createdAt(user.getCreatedAt())
            .build();
    }
}