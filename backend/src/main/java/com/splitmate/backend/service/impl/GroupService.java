package com.splitmate.backend.service.impl;

import com.splitmate.backend.dto.request.GroupRequest;
import com.splitmate.backend.dto.response.ApiResponse;
import com.splitmate.backend.entity.*;
import com.splitmate.backend.exception.*;
import com.splitmate.backend.repository.*;
import com.splitmate.backend.service.BalanceCalculatorService;
import com.splitmate.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;
    private final BalanceCalculatorService balanceCalculatorService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public ApiResponse.GroupResponse createGroup(GroupRequest.Create request, Long creatorId) {
        User creator = userRepository.findById(creatorId)
            .orElseThrow(() -> new ResourceNotFoundException("User", creatorId));

        Group group = Group.builder()
            .name(request.getName())
            .description(request.getDescription())
            .category(request.getCategory() != null ? request.getCategory() : "OTHER")
            .createdBy(creator)
            .build();

        group = groupRepository.save(group);

        Set<String> addedEmails = new HashSet<>();
        addedEmails.add(creator.getEmail().toLowerCase());

        GroupMember creatorMember = GroupMember.builder()
            .group(group).user(creator).build();
        groupMemberRepository.save(creatorMember);

        if (request.getMemberEmails() != null) {
            for (String email : request.getMemberEmails()) {
                String lowerEmail = email.toLowerCase().trim();
                if (addedEmails.contains(lowerEmail)) continue;
                addedEmails.add(lowerEmail);

                boolean isNewUser = !userRepository.existsByEmail(lowerEmail);

                User memberUser = userRepository.findByEmail(lowerEmail)
                    .orElseGet(() -> {
                        String[] colors = {"#6366f1", "#ec4899", "#f59e0b",
                            "#10b981", "#3b82f6", "#8b5cf6"};
                        String color = colors[Math.abs(lowerEmail.charAt(0)) % colors.length];
                        User newUser = User.builder()
                            .name(lowerEmail.split("@")[0])
                            .email(lowerEmail)
                            .password(passwordEncoder.encode("Splitmate@123"))
                            .avatarColor(color)
                            .build();
                        return userRepository.save(newUser);
                    });

                GroupMember member = GroupMember.builder()
                    .group(group).user(memberUser).build();
                groupMemberRepository.save(member);

                // Send email notification
                emailService.sendMemberAddedEmail(
                    memberUser.getEmail(),
                    memberUser.getName(),
                    group.getName(),
                    isNewUser
                );
            }
        }

        return buildGroupResponse(groupRepository.findById(group.getId()).get());
    }

    public List<ApiResponse.GroupResponse> getUserGroups(Long userId) {
        return groupRepository.findGroupsByUserId(userId)
            .stream()
            .map(this::buildGroupResponse)
            .collect(Collectors.toList());
    }

    public ApiResponse.GroupResponse getGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));
        assertMembership(groupId, userId);
        return buildGroupResponse(group);
    }

    @Transactional
    public ApiResponse.GroupResponse updateGroup(Long groupId,
            GroupRequest.Update request, Long userId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));

        if (!group.getCreatedBy().getId().equals(userId)) {
            throw new UnauthorizedException("Only group creator can update group");
        }

        if (request.getName() != null) group.setName(request.getName());
        if (request.getDescription() != null) group.setDescription(request.getDescription());
        if (request.getCategory() != null) group.setCategory(request.getCategory());

        return buildGroupResponse(groupRepository.save(group));
    }

    @Transactional
    public void addMember(Long groupId, GroupRequest.AddMember request, Long requesterId) {
        assertMembership(groupId, requesterId);

        String lowerEmail = request.getEmail().toLowerCase();

        boolean isNewUser = !userRepository.existsByEmail(lowerEmail);

        User user = userRepository.findByEmail(lowerEmail)
            .orElseGet(() -> {
                String[] colors = {"#6366f1", "#ec4899", "#f59e0b",
                    "#10b981", "#3b82f6", "#8b5cf6"};
                String color = colors[Math.abs(lowerEmail.charAt(0)) % colors.length];
                User newUser = User.builder()
                    .name(lowerEmail.split("@")[0])
                    .email(lowerEmail)
                    .password(passwordEncoder.encode("Splitmate@123"))
                    .avatarColor(color)
                    .build();
                return userRepository.save(newUser);
            });

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new DuplicateResourceException("User is already a member of this group");
        }

        Group group = groupRepository.findById(groupId).get();
        GroupMember member = GroupMember.builder()
            .group(group).user(user).build();
        groupMemberRepository.save(member);

        // Send email notification
        emailService.sendMemberAddedEmail(
            user.getEmail(),
            user.getName(),
            group.getName(),
            isNewUser
        );
    }

    @Transactional
    public void removeMember(Long groupId, Long memberId, Long requesterId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));

        if (!group.getCreatedBy().getId().equals(requesterId)
                && !requesterId.equals(memberId)) {
            throw new UnauthorizedException("Not authorized to remove this member");
        }

        if (group.getCreatedBy().getId().equals(memberId)) {
            throw new BadRequestException("Cannot remove the group creator");
        }

        groupMemberRepository.findByGroupIdAndUserId(groupId, memberId)
            .ifPresent(groupMemberRepository::delete);
    }

    public ApiResponse.GroupSummaryResponse getGroupSummary(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));
        assertMembership(groupId, userId);

        List<ApiResponse.BalanceResponse> balances =
            balanceCalculatorService.calculateBalances(groupId);
        List<ApiResponse.DebtResponse> debts =
            balanceCalculatorService.calculateSimplifiedDebts(groupId);
        BigDecimal totalAmount = expenseRepository.sumAmountByGroupId(groupId);

        List<ApiResponse.SettlementResponse> recentSettlements = settlementRepository
            .findByGroupIdOrderByCreatedAtDesc(groupId)
            .stream().limit(5)
            .map(this::mapToSettlementResponse)
            .collect(Collectors.toList());

        return ApiResponse.GroupSummaryResponse.builder()
            .group(buildGroupResponse(group))
            .balances(balances)
            .simplifiedDebts(debts)
            .recentSettlements(recentSettlements)
            .totalGroupExpense(totalAmount)
            .build();
    }

    private ApiResponse.GroupResponse buildGroupResponse(Group group) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(group.getId());
        BigDecimal totalAmount = expenseRepository.sumAmountByGroupId(group.getId());
        long expenseCount = groupRepository.countExpensesByGroupId(group.getId());

        return ApiResponse.GroupResponse.builder()
            .id(group.getId())
            .name(group.getName())
            .description(group.getDescription())
            .category(group.getCategory())
            .createdBy(mapToUserResponse(group.getCreatedBy()))
            .members(members.stream()
                .map(m -> mapToUserResponse(m.getUser()))
                .collect(Collectors.toList()))
            .expenseCount(expenseCount)
            .totalAmount(totalAmount)
            .createdAt(group.getCreatedAt())
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

    private ApiResponse.SettlementResponse mapToSettlementResponse(Settlement s) {
        return ApiResponse.SettlementResponse.builder()
            .id(s.getId())
            .groupId(s.getGroup().getId())
            .fromUser(mapToUserResponse(s.getFromUser()))
            .toUser(mapToUserResponse(s.getToUser()))
            .amount(s.getAmount())
            .note(s.getNote())
            .status(s.getStatus())
            .settledAt(s.getSettledAt())
            .createdAt(s.getCreatedAt())
            .build();
    }

    private void assertMembership(Long groupId, Long userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
    }
}