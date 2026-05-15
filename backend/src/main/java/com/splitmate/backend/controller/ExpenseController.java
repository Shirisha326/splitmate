package com.splitmate.backend.controller;

import com.splitmate.backend.config.CurrentUserHelper;
import com.splitmate.backend.dto.request.ExpenseRequest;
import com.splitmate.backend.dto.response.ApiResponse;
import com.splitmate.backend.service.impl.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups/{groupId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CurrentUserHelper currentUserHelper;

    @PostMapping
    public ResponseEntity<ApiResponse.Success<ApiResponse.ExpenseResponse>> createExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody ExpenseRequest.Create request) {
        Long userId = currentUserHelper.getCurrentUserId();
        ApiResponse.ExpenseResponse response = expenseService.createExpense(groupId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.Success.of("Expense added successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse.Success<List<ApiResponse.ExpenseResponse>>> getGroupExpenses(
            @PathVariable Long groupId) {
        Long userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.Success.of(expenseService.getGroupExpenses(groupId, userId)));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ApiResponse.Success<ApiResponse.ExpenseResponse>> getExpense(
            @PathVariable Long groupId,
            @PathVariable Long expenseId) {
        Long userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.Success.of(expenseService.getExpense(expenseId, userId)));
    }

    @PatchMapping("/{expenseId}")
    public ResponseEntity<ApiResponse.Success<ApiResponse.ExpenseResponse>> updateExpense(
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            @RequestBody ExpenseRequest.Update request) {
        Long userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.Success.of(expenseService.updateExpense(expenseId, request, userId)));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ApiResponse.Success<Void>> deleteExpense(
            @PathVariable Long groupId,
            @PathVariable Long expenseId) {
        Long userId = currentUserHelper.getCurrentUserId();
        expenseService.deleteExpense(expenseId, userId);
        return ResponseEntity.ok(ApiResponse.Success.of("Expense deleted", null));
    }
}