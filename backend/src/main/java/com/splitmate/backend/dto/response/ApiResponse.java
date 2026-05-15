package com.splitmate.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ApiResponse {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Success<T> {
        private boolean success = true;
        private String message;
        private T data;

        public static <T> Success<T> of(T data) {
            return Success.<T>builder().success(true).data(data).build();
        }

        public static <T> Success<T> of(String message, T data) {
            return Success.<T>builder().success(true).message(message).data(data).build();
        }
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Error {
        private boolean success = false;
        private String message;
        private Map<String, String> errors;
        private int status;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String tokenType = "Bearer";
        private UserResponse user;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private String avatarColor;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class GroupResponse {
        private Long id;
        private String name;
        private String description;
        private String category;
        private UserResponse createdBy;
        private List<UserResponse> members;
        private long expenseCount;
        private BigDecimal totalAmount;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ExpenseResponse {
        private Long id;
        private Long groupId;
        private String description;
        private BigDecimal amount;
        private UserResponse paidBy;
        private String splitType;
        private String category;
        private List<ParticipantResponse> participants;
        private UserResponse createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ParticipantResponse {
        private Long userId;
        private String userName;
        private String userEmail;
        private BigDecimal shareAmount;
        private BigDecimal percentage;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BalanceResponse {
        private Long userId;
        private String userName;
        private String userEmail;
        private String avatarColor;
        private BigDecimal netBalance;
        private BigDecimal totalPaid;
        private BigDecimal totalShare;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DebtResponse {
        private Long fromUserId;
        private String fromUserName;
        private String fromUserEmail;
        private String fromAvatarColor;
        private Long toUserId;
        private String toUserName;
        private String toUserEmail;
        private String toAvatarColor;
        private BigDecimal amount;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SettlementResponse {
        private Long id;
        private Long groupId;
        private UserResponse fromUser;
        private UserResponse toUser;
        private BigDecimal amount;
        private String note;
        private String status;
        private LocalDateTime settledAt;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class GroupSummaryResponse {
        private GroupResponse group;
        private List<BalanceResponse> balances;
        private List<DebtResponse> simplifiedDebts;
        private List<SettlementResponse> recentSettlements;
        private BigDecimal totalGroupExpense;
    }
}