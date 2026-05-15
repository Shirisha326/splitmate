package com.splitmate.backend.controller;

import com.splitmate.backend.config.CurrentUserHelper;
import com.splitmate.backend.dto.request.GroupRequest;
import com.splitmate.backend.dto.response.ApiResponse;
import com.splitmate.backend.service.impl.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final CurrentUserHelper currentUserHelper;

    @PostMapping
    public ResponseEntity<ApiResponse.Success<ApiResponse.GroupResponse>> createGroup(
            @Valid @RequestBody GroupRequest.Create request) {
        Long userId = currentUserHelper.getCurrentUserId();
        ApiResponse.GroupResponse response = groupService.createGroup(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.Success.of("Group created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse.Success<List<ApiResponse.GroupResponse>>> getUserGroups() {
        Long userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.Success.of(groupService.getUserGroups(userId)));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse.Success<ApiResponse.GroupResponse>> getGroup(
            @PathVariable Long groupId) {
        Long userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.Success.of(groupService.getGroup(groupId, userId)));
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<ApiResponse.Success<ApiResponse.GroupResponse>> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody GroupRequest.Update request) {
        Long userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.Success.of(groupService.updateGroup(groupId, request, userId)));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse.Success<Void>> addMember(
            @PathVariable Long groupId,
            @Valid @RequestBody GroupRequest.AddMember request) {
        Long userId = currentUserHelper.getCurrentUserId();
        groupService.addMember(groupId, request, userId);
        return ResponseEntity.ok(ApiResponse.Success.of("Member added successfully", null));
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<ApiResponse.Success<Void>> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        Long userId = currentUserHelper.getCurrentUserId();
        groupService.removeMember(groupId, memberId, userId);
        return ResponseEntity.ok(ApiResponse.Success.of("Member removed", null));
    }

    @GetMapping("/{groupId}/summary")
    public ResponseEntity<ApiResponse.Success<ApiResponse.GroupSummaryResponse>> getGroupSummary(
            @PathVariable Long groupId) {
        Long userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.Success.of(groupService.getGroupSummary(groupId, userId)));
    }
}