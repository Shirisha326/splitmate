package com.splitmate.backend.controller;

import com.splitmate.backend.config.CurrentUserHelper;
import com.splitmate.backend.dto.response.ApiResponse;
import com.splitmate.backend.entity.User;
import com.splitmate.backend.exception.BadRequestException;
import com.splitmate.backend.repository.UserRepository;
import com.splitmate.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final CurrentUserHelper currentUserHelper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // Store OTPs temporarily
    // In production use Redis with expiry
    private static final Map<String, String> otpStore = new ConcurrentHashMap<>();

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

        return ResponseEntity.ok(
            ApiResponse.Success.of("Password changed successfully", null)
        );
    }

    // =========================
    // SEND OTP
    // =========================

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<ApiResponse.Success<Void>> sendOtp(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }

        User user = userRepository.findByEmail(email.toLowerCase().trim())
            .orElseThrow(() ->
                new BadRequestException("No account found with this email"));

        // Generate 6 digit OTP
        String otp = String.format(
            "%06d",
            new java.util.Random().nextInt(999999)
        );

        otpStore.put(email.toLowerCase(), otp);

        // Send email
        emailService.sendOtpEmail(
            user.getEmail(),
            user.getName(),
            otp
        );

        return ResponseEntity.ok(
            ApiResponse.Success.of("OTP sent to your email", null)
        );
    }

    // =========================
    // RESET PASSWORD WITH OTP
    // =========================

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiResponse.Success<Void>> resetPasswordWithOtp(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        if (email == null || otp == null || newPassword == null) {
            throw new BadRequestException(
                "Email, OTP and new password are required"
            );
        }

        if (newPassword.length() < 6) {
            throw new BadRequestException(
                "Password must be at least 6 characters"
            );
        }

        String storedOtp = otpStore.get(email.toLowerCase());

        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(email.toLowerCase().trim())
            .orElseThrow(() ->
                new BadRequestException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Remove OTP after successful use
        otpStore.remove(email.toLowerCase());

        return ResponseEntity.ok(
            ApiResponse.Success.of(
                "Password reset successfully!",
                null
            )
        );
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