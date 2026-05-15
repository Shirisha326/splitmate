package com.splitmate.backend.service.impl;

import com.splitmate.backend.dto.request.ExpenseRequest;
import com.splitmate.backend.dto.response.ApiResponse;
import com.splitmate.backend.entity.*;
import com.splitmate.backend.exception.*;
import com.splitmate.backend.repository.*;
import com.splitmate.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseParticipantRepository participantRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public ApiResponse.ExpenseResponse createExpense(Long groupId,
            ExpenseRequest.Create request, Long creatorId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));

        assertMembership(groupId, creatorId);

        User paidBy = userRepository.findById(request.getPaidById())
            .orElseThrow(() -> new ResourceNotFoundException("User", request.getPaidById()));

        assertMembership(groupId, paidBy.getId());

        User creator = userRepository.findById(creatorId).get();

        Expense expense = Expense.builder()
            .group(group)
            .description(request.getDescription())
            .amount(request.getAmount())
            .paidBy(paidBy)
            .splitType(request.getSplitType())
            .category(request.getCategory() != null ? request.getCategory() : "OTHER")
            .createdBy(creator)
            .build();

        expense = expenseRepository.save(expense);

        List<ExpenseParticipant> participants = buildParticipants(expense, request, groupId);
        participantRepository.saveAll(participants);

        // Send email to all participants except creator
        Expense savedExpense = expenseRepository.findById(expense.getId()).get();
        List<ExpenseParticipant> savedParticipants =
            participantRepository.findByExpenseId(savedExpense.getId());

        for (ExpenseParticipant p : savedParticipants) {
            if (!p.getUser().getId().equals(creatorId)) {
                emailService.sendExpenseAddedEmail(
                    p.getUser().getEmail(),
                    p.getUser().getName(),
                    group.getName(),
                    expense.getDescription(),
                    expense.getAmount().toString(),
                    paidBy.getName(),
                    p.getShareAmount().toString()
                );
            }
        }

        return buildExpenseResponse(savedExpense);
    }

    @Transactional
    public ApiResponse.ExpenseResponse updateExpense(Long expenseId,
            ExpenseRequest.Update request, Long userId) {
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new ResourceNotFoundException("Expense", expenseId));

        assertMembership(expense.getGroup().getId(), userId);

        if (request.getDescription() != null) expense.setDescription(request.getDescription());
        if (request.getCategory() != null) expense.setCategory(request.getCategory());

        if (request.getPaidById() != null) {
            User paidBy = userRepository.findById(request.getPaidById())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getPaidById()));
            expense.setPaidBy(paidBy);
        }

        if (request.getAmount() != null) expense.setAmount(request.getAmount());
        if (request.getSplitType() != null) expense.setSplitType(request.getSplitType());

        if (request.getAmount() != null || request.getSplitType() != null ||
            request.getParticipantIds() != null || request.getExactSplits() != null) {

            participantRepository.deleteAll(
                participantRepository.findByExpenseId(expense.getId()));

            ExpenseRequest.Create createReq = new ExpenseRequest.Create();
            createReq.setAmount(expense.getAmount());
            createReq.setSplitType(expense.getSplitType());
            createReq.setParticipantIds(request.getParticipantIds());
            createReq.setExactSplits(request.getExactSplits());
            createReq.setPercentageSplits(request.getPercentageSplits());

            List<ExpenseParticipant> participants = buildParticipants(
                expense, createReq, expense.getGroup().getId());
            participantRepository.saveAll(participants);
        }

        return buildExpenseResponse(expenseRepository.save(expense));
    }

    @Transactional
    public void deleteExpense(Long expenseId, Long userId) {
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new ResourceNotFoundException("Expense", expenseId));
        assertMembership(expense.getGroup().getId(), userId);
        expenseRepository.delete(expense);
    }

    public List<ApiResponse.ExpenseResponse> getGroupExpenses(Long groupId, Long userId) {
        assertMembership(groupId, userId);
        return expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId)
            .stream()
            .map(this::buildExpenseResponse)
            .collect(Collectors.toList());
    }

    public ApiResponse.ExpenseResponse getExpense(Long expenseId, Long userId) {
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new ResourceNotFoundException("Expense", expenseId));
        assertMembership(expense.getGroup().getId(), userId);
        return buildExpenseResponse(expense);
    }

    private List<ExpenseParticipant> buildParticipants(Expense expense,
            ExpenseRequest.Create request, Long groupId) {
        BigDecimal amount = expense.getAmount();
        String splitType = expense.getSplitType();

        return switch (splitType) {
            case "EQUAL" -> buildEqualSplit(expense, request, groupId, amount);
            case "EXACT" -> buildExactSplit(expense, request);
            case "PERCENTAGE" -> buildPercentageSplit(expense, request, amount);
            default -> throw new BadRequestException("Invalid split type: " + splitType);
        };
    }

    private List<ExpenseParticipant> buildEqualSplit(Expense expense,
            ExpenseRequest.Create request, Long groupId, BigDecimal amount) {
        List<Long> participantIds = request.getParticipantIds();
        if (participantIds == null || participantIds.isEmpty()) {
            participantIds = groupMemberRepository.findUserIdsByGroupId(groupId);
        }

        int count = participantIds.size();
        BigDecimal share = amount.divide(BigDecimal.valueOf(count), 2, RoundingMode.FLOOR);
        BigDecimal remainder = amount.subtract(share.multiply(BigDecimal.valueOf(count)));

        List<ExpenseParticipant> participants = new ArrayList<>();
        for (int i = 0; i < participantIds.size(); i++) {
            Long userId = participantIds.get(i);
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
            BigDecimal shareAmount = (i == 0) ? share.add(remainder) : share;
            participants.add(ExpenseParticipant.builder()
                .expense(expense).user(user).shareAmount(shareAmount).build());
        }
        return participants;
    }

    private List<ExpenseParticipant> buildExactSplit(Expense expense,
            ExpenseRequest.Create request) {
        if (request.getExactSplits() == null || request.getExactSplits().isEmpty()) {
            throw new BadRequestException("Exact splits are required for EXACT split type");
        }

        BigDecimal total = request.getExactSplits().values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(expense.getAmount()) != 0) {
            throw new BadRequestException("Sum of exact splits must equal expense amount");
        }

        List<ExpenseParticipant> participants = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : request.getExactSplits().entrySet()) {
            User user = userRepository.findById(entry.getKey())
                .orElseThrow(() -> new ResourceNotFoundException("User", entry.getKey()));
            participants.add(ExpenseParticipant.builder()
                .expense(expense).user(user)
                .shareAmount(entry.getValue()).build());
        }
        return participants;
    }

    private List<ExpenseParticipant> buildPercentageSplit(Expense expense,
            ExpenseRequest.Create request, BigDecimal amount) {
        if (request.getPercentageSplits() == null || request.getPercentageSplits().isEmpty()) {
            throw new BadRequestException("Percentage splits required for PERCENTAGE split type");
        }

        BigDecimal totalPct = request.getPercentageSplits().values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPct.compareTo(new BigDecimal("100")) != 0) {
            throw new BadRequestException("Percentages must sum to 100");
        }

        List<ExpenseParticipant> participants = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : request.getPercentageSplits().entrySet()) {
            User user = userRepository.findById(entry.getKey())
                .orElseThrow(() -> new ResourceNotFoundException("User", entry.getKey()));
            BigDecimal shareAmount = amount.multiply(entry.getValue())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            participants.add(ExpenseParticipant.builder()
                .expense(expense).user(user)
                .shareAmount(shareAmount)
                .percentage(entry.getValue()).build());
        }
        return participants;
    }

    private ApiResponse.ExpenseResponse buildExpenseResponse(Expense expense) {
        List<ExpenseParticipant> participants =
            participantRepository.findByExpenseId(expense.getId());

        return ApiResponse.ExpenseResponse.builder()
            .id(expense.getId())
            .groupId(expense.getGroup().getId())
            .description(expense.getDescription())
            .amount(expense.getAmount())
            .paidBy(mapToUserResponse(expense.getPaidBy()))
            .splitType(expense.getSplitType())
            .category(expense.getCategory())
            .participants(participants.stream().map(p ->
                ApiResponse.ParticipantResponse.builder()
                    .userId(p.getUser().getId())
                    .userName(p.getUser().getName())
                    .userEmail(p.getUser().getEmail())
                    .shareAmount(p.getShareAmount())
                    .percentage(p.getPercentage())
                    .build()
            ).collect(Collectors.toList()))
            .createdBy(mapToUserResponse(expense.getCreatedBy()))
            .createdAt(expense.getCreatedAt())
            .updatedAt(expense.getUpdatedAt())
            .build();
    }

    private ApiResponse.UserResponse mapToUserResponse(User user) {
        return ApiResponse.UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .avatarColor(user.getAvatarColor())
            .build();
    }

    private void assertMembership(Long groupId, Long userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
    }
}