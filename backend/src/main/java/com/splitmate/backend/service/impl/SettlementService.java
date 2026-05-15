package com.splitmate.backend.service.impl;

import com.splitmate.backend.dto.request.SettlementRequest;
import com.splitmate.backend.dto.response.ApiResponse;
import com.splitmate.backend.entity.*;
import com.splitmate.backend.exception.BadRequestException;
import com.splitmate.backend.exception.ResourceNotFoundException;
import com.splitmate.backend.exception.UnauthorizedException;
import com.splitmate.backend.repository.*;
import com.splitmate.backend.service.EmailService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public ApiResponse.SettlementResponse createSettlement(Long groupId, SettlementRequest request, Long fromUserId) {
        assertMembership(groupId, fromUserId);

        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));

        User fromUser = userRepository.findById(fromUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", fromUserId));

        User toUser = userRepository.findById(request.getToUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User", request.getToUserId()));

        assertMembership(groupId, toUser.getId());

        if (fromUserId.equals(toUser.getId())) {
            throw new BadRequestException("Cannot settle with yourself");
        }

        Settlement settlement = Settlement.builder()
            .group(group)
            .fromUser(fromUser)
            .toUser(toUser)
            .amount(request.getAmount())
            .note(request.getNote())
            .status("COMPLETED")
            .settledAt(LocalDateTime.now())
            .build();

            // Send email to person who received payment
emailService.sendSettleUpEmail(
    toUser.getEmail(),
    toUser.getName(),
    fromUser.getName(),
    request.getAmount().toString(),
    group.getName()
);

return mapToResponse(settlementRepository.save(settlement));

    
    }

    public List<ApiResponse.SettlementResponse> getGroupSettlements(Long groupId, Long userId) {
        assertMembership(groupId, userId);
        return settlementRepository.findByGroupIdOrderByCreatedAtDesc(groupId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSettlement(Long settlementId, Long userId) {
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new ResourceNotFoundException("Settlement", settlementId));

        if (!settlement.getFromUser().getId().equals(userId)) {
            throw new UnauthorizedException("Only the payer can delete this settlement");
        }

        settlementRepository.delete(settlement);
    }

    private ApiResponse.SettlementResponse mapToResponse(Settlement s) {
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
