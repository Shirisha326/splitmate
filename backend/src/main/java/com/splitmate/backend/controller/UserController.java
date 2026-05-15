package com.splitmate.backend.controller;

import com.splitmate.backend.config.CurrentUserHelper;
import com.splitmate.backend.dto.response.ApiResponse;
import com.splitmate.backend.entity.User;
import com.splitmate.backend.exception.BadRequestException;
import com.splitmate.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final CurrentUserHelper currentUserHelper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse.Success<ApiResponse.UserResponse>> getCurrentUser() {
        User user = currentUserHelper.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.Success.of(mapToResponse(user)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse.Success<List<ApiResponse.UserResponse>>> searchUsers(
            @RequestParam String email) {
        return userRepository.findByEmail(email.toLowerCase())
            .map(user -> ResponseEntity.ok(
                ApiResponse.Success.of(List.of(mapToResponse(user)))))
            .orElse(ResponseEntity.ok(ApiResponse.Success.of(List.of())));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse.Success<Void>> changePassword(
            @RequestBody Map<String, String> request) {
        User user = currentUserHelper.getCurrentUser();
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new BadRequestException("New password must be at least 6 characters");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.Success.of("Password changed successfully", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse.Success<Void>> forgotPassword(
            @RequestBody Map<String, String> request) {
        String email = request.get("email");
        String name = request.get("name");
        String newPassword = request.get("newPassword");

        if (email == null || name == null || newPassword == null) {
            throw new BadRequestException("Email, name and new password are required");
        }

        if (newPassword.length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }

        User user = userRepository.findByEmail(email.toLowerCase().trim())
            .orElseThrow(() -> new BadRequestException("No account found with this email"));

        if (!user.getName().toLowerCase().trim()
                .equals(name.toLowerCase().trim())) {
            throw new BadRequestException("Name does not match our records");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.Success.of("Password reset successfully!", null));
    }

    private ApiResponse.UserResponse mapToResponse(User user) {
        return ApiResponse.UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .avatarColor(user.getAvatarColor())
            .createdAt(user.getCreatedAt())
            .build();
    }
}