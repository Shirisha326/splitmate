package com.splitmate.backend.controller;

import com.splitmate.backend.config.CurrentUserHelper;
import com.splitmate.backend.dto.request.SettlementRequest;
import com.splitmate.backend.dto.response.ApiResponse;
import com.splitmate.backend.service.BalanceCalculatorService;
import com.splitmate.backend.service.impl.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups/{groupId}")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;
    private final BalanceCalculatorService balanceCalculatorService;
    private final CurrentUserHelper currentUserHelper;

    @PostMapping("/settlements")
    public ResponseEntity<ApiResponse.Success<ApiResponse.SettlementResponse>> createSettlement(
            @PathVariable Long groupId,
            @Valid @RequestBody SettlementRequest request) {
        Long userId = currentUserHelper.getCurrentUserId();
        ApiResponse.SettlementResponse response = settlementService.createSettlement(groupId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.Success.of("Settlement recorded", response));
    }

    @GetMapping("/settlements")
    public ResponseEntity<ApiResponse.Success<List<ApiResponse.SettlementResponse>>> getSettlements(
            @PathVariable Long groupId) {
        Long userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.Success.of(
            settlementService.getGroupSettlements(groupId, userId)));
    }

    @DeleteMapping("/settlements/{settlementId}")
    public ResponseEntity<ApiResponse.Success<Void>> deleteSettlement(
            @PathVariable Long groupId,
            @PathVariable Long settlementId) {
        Long userId = currentUserHelper.getCurrentUserId();
        settlementService.deleteSettlement(settlementId, userId);
        return ResponseEntity.ok(ApiResponse.Success.of("Settlement deleted", null));
    }

    @GetMapping("/balances")
    public ResponseEntity<ApiResponse.Success<List<ApiResponse.BalanceResponse>>> getBalances(
            @PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.Success.of(
            balanceCalculatorService.calculateBalances(groupId)));
    }

    @GetMapping("/debts")
    public ResponseEntity<ApiResponse.Success<List<ApiResponse.DebtResponse>>> getDebts(
            @PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.Success.of(
            balanceCalculatorService.calculateSimplifiedDebts(groupId)));
    }
}