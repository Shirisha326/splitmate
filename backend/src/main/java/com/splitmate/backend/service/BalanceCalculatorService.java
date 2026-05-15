package com.splitmate.backend.service;

import com.splitmate.backend.dto.response.ApiResponse;
import com.splitmate.backend.entity.*;
import com.splitmate.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceCalculatorService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseParticipantRepository participantRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final SettlementRepository settlementRepository;

    public List<ApiResponse.BalanceResponse> calculateBalances(Long groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        Map<Long, BigDecimal> netBalances = new HashMap<>();

        members.forEach(m -> netBalances.put(m.getUser().getId(), BigDecimal.ZERO));

        List<Expense> expenses = expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId);

        for (Expense expense : expenses) {
            Long payerId = expense.getPaidBy().getId();
            BigDecimal amount = expense.getAmount();
            netBalances.merge(payerId, amount, BigDecimal::add);

            List<ExpenseParticipant> participants = participantRepository.findByExpenseId(expense.getId());
            for (ExpenseParticipant participant : participants) {
                netBalances.merge(participant.getUser().getId(),
                    participant.getShareAmount().negate(), BigDecimal::add);
            }
        }

        List<Settlement> completedSettlements = settlementRepository.findCompletedByGroupId(groupId);
        for (Settlement settlement : completedSettlements) {
            netBalances.merge(settlement.getFromUser().getId(), settlement.getAmount(), BigDecimal::add);
            netBalances.merge(settlement.getToUser().getId(), settlement.getAmount().negate(), BigDecimal::add);
        }

        Map<Long, User> userMap = members.stream()
            .collect(Collectors.toMap(m -> m.getUser().getId(), GroupMember::getUser));

        return netBalances.entrySet().stream()
            .map(entry -> {
                User user = userMap.get(entry.getKey());
                BigDecimal net = entry.getValue().setScale(2, RoundingMode.HALF_UP);
                BigDecimal totalPaid = expenseRepository.sumAmountPaidByUserInGroup(groupId, user.getId());
                BigDecimal totalShare = participantRepository.sumShareAmountByGroupIdAndUserId(groupId, user.getId());

                return ApiResponse.BalanceResponse.builder()
                    .userId(user.getId())
                    .userName(user.getName())
                    .userEmail(user.getEmail())
                    .avatarColor(user.getAvatarColor())
                    .netBalance(net)
                    .totalPaid(totalPaid.setScale(2, RoundingMode.HALF_UP))
                    .totalShare(totalShare.setScale(2, RoundingMode.HALF_UP))
                    .build();
            })
            .collect(Collectors.toList());
    }

    public List<ApiResponse.DebtResponse> calculateSimplifiedDebts(Long groupId) {
        List<ApiResponse.BalanceResponse> balances = calculateBalances(groupId);

        PriorityQueue<long[]> creditors = new PriorityQueue<>(
            (a, b) -> Double.compare(b[1], a[1]));
        PriorityQueue<long[]> debtors = new PriorityQueue<>(
            (a, b) -> Double.compare(a[1], b[1]));

        Map<Long, ApiResponse.BalanceResponse> balanceMap = new HashMap<>();

        for (ApiResponse.BalanceResponse balance : balances) {
            balanceMap.put(balance.getUserId(), balance);
            int amountCents = balance.getNetBalance()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).intValue();

            if (amountCents > 0) {
                creditors.offer(new long[]{balance.getUserId(), amountCents});
            } else if (amountCents < 0) {
                debtors.offer(new long[]{balance.getUserId(), amountCents});
            }
        }

        List<ApiResponse.DebtResponse> debts = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            long[] creditor = creditors.poll();
            long[] debtor = debtors.poll();

            long creditAmount = creditor[1];
            long debitAmount = Math.abs(debtor[1]);
            long settledAmount = Math.min(creditAmount, debitAmount);

            ApiResponse.BalanceResponse fromUser = balanceMap.get(debtor[0]);
            ApiResponse.BalanceResponse toUser = balanceMap.get(creditor[0]);

            BigDecimal amount = BigDecimal.valueOf(settledAmount, 2);

            debts.add(ApiResponse.DebtResponse.builder()
                .fromUserId(fromUser.getUserId())
                .fromUserName(fromUser.getUserName())
                .fromUserEmail(fromUser.getUserEmail())
                .fromAvatarColor(fromUser.getAvatarColor())
                .toUserId(toUser.getUserId())
                .toUserName(toUser.getUserName())
                .toUserEmail(toUser.getUserEmail())
                .toAvatarColor(toUser.getAvatarColor())
                .amount(amount)
                .build());

            long remaining = creditAmount - debitAmount;
            if (remaining > 0) {
                creditors.offer(new long[]{creditor[0], remaining});
            } else if (remaining < 0) {
                debtors.offer(new long[]{debtor[0], remaining});
            }
        }

        return debts;
    }
}