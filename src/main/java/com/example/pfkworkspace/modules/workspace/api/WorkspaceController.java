package com.example.pfkworkspace.modules.workspace.api;

import com.example.pfkworkspace.common.api.ApiResponse;
import com.example.pfkworkspace.modules.workspace.api.dto.*;
import com.example.pfkworkspace.modules.workspace.application.WorkspaceInvitationService;
import com.example.pfkworkspace.modules.workspace.application.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspace")
@RequiredArgsConstructor
public class WorkspaceController {
  private final WorkspaceService workspaceService;
  private final WorkspaceInvitationService workspaceInvitationService;

  // --- Workspace Operations ---

  @PostMapping
  public ResponseEntity<ApiResponse> createWorkspace(
      @Valid @RequestBody CreateWorkspaceRequestDto createWorkspaceRequestDto) {
    CreateWorkspaceResponseDto responseDto =
        workspaceService.createWorkspace(createWorkspaceRequestDto);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(responseDto)
            .message(HttpStatus.CREATED.name())
            .build();

    return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<ApiResponse> getWorkspaces() {
    List<WorkspaceSummaryDto> workspaces = workspaceService.getWorkspaces();
    ApiResponse apiResponse =
        ApiResponse.builder().success(true).data(workspaces).message(HttpStatus.OK.name()).build();

    return ResponseEntity.ok(apiResponse);
  }

  @GetMapping("/{workspaceId}")
  public ResponseEntity<ApiResponse> getWorkspaceDetail(@PathVariable UUID workspaceId) {
    WorkspaceDetailDto workspaceDetail = workspaceService.getWorkspaceDetail(workspaceId);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(workspaceDetail)
            .message(HttpStatus.OK.name())
            .build();

    return ResponseEntity.ok(apiResponse);
  }

  @PutMapping("/{workspaceId}")
  public ResponseEntity<ApiResponse> updateWorkspace(
      @PathVariable UUID workspaceId,
      @Valid @RequestBody UpdateWorkspaceRequestDto updateWorkspaceRequestDto) {
    UpdateWorkspaceResponseDto responseDto =
        workspaceService.updateWorkspace(workspaceId, updateWorkspaceRequestDto);
    ApiResponse apiResponse =
        ApiResponse.builder().success(true).data(responseDto).message(HttpStatus.OK.name()).build();

    return ResponseEntity.ok(apiResponse);
  }

  @DeleteMapping("/{workspaceId}")
  public ResponseEntity<ApiResponse> deleteWorkspace(@PathVariable UUID workspaceId) {
    workspaceService.deleteWorkspace(workspaceId);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(null)
            .message("Workspace deleted successfully")
            .build();

    return ResponseEntity.ok(apiResponse);
  }

  // --- Member Operations ---

  @PutMapping("/{workspaceId}/members")
  public ResponseEntity<ApiResponse> updateMemberRole(
      @PathVariable UUID workspaceId,
      @Valid @RequestBody UpdateMemberRoleRequestDto updateMemberRoleRequestDto) {
    workspaceService.updateMemberRole(
        workspaceId, updateMemberRoleRequestDto.getMemberId(), updateMemberRoleRequestDto.getRole());
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .message("Member role updated successfully")
            .data(null)
            .build();
    return ResponseEntity.ok(apiResponse);
  }

  @DeleteMapping("/{workspaceId}/members/{userId}")
  public ResponseEntity<ApiResponse> removeMemberFromWorkspace(
      @PathVariable UUID workspaceId, @PathVariable UUID userId) {
    workspaceService.removeUserFromWorkspace(workspaceId, userId);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .message("Member removed from workspace successfully")
            .data(null)
            .build();
    return ResponseEntity.ok(apiResponse);
  }

  // --- Invitation Operations ---

  @PostMapping("/{workspaceId}")
  public ResponseEntity<ApiResponse> addMemberToWorkspace(
      @PathVariable UUID workspaceId,
      @RequestBody @Valid CreateInvitationRequestDto createInvitationRequestDto) {
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .message("Member sent request successfully")
            .data(
                workspaceInvitationService.addMemberToWorkspace(
                    createInvitationRequestDto, workspaceId))
            .build();
    return ResponseEntity.ok(apiResponse);
  }

  @PostMapping("/invitations/{invitationId}/resend")
  public ResponseEntity<ApiResponse> resendInvitation(@PathVariable UUID invitationId) {
    workspaceInvitationService.resendInvitation(invitationId);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .message("Invitation resent successfully")
            .data(null)
            .build();
    return ResponseEntity.ok(apiResponse);
  }

  @DeleteMapping("/invitations/{invitationId}")
  public ResponseEntity<ApiResponse> revokeInvitation(@PathVariable UUID invitationId) {
    workspaceInvitationService.revokeInvitation(invitationId);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .message("Invitation revoked successfully")
            .data(null)
            .build();
    return ResponseEntity.ok(apiResponse);
  }

  @PostMapping("/{workspaceId}/invitations/accept")
  public ResponseEntity<ApiResponse> acceptInvitation(@RequestParam String token) {
    workspaceInvitationService.acceptInvitation(token);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .message("Invitation accepted successfully")
            .data(null)
            .build();
    return ResponseEntity.ok(apiResponse);
  }

  @PostMapping("/{workspaceId}/invitations/reject")
  public ResponseEntity<ApiResponse> rejectInvitation(@RequestParam String token) {
    workspaceInvitationService.declineInvitation(token);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .message("Invitation rejected successfully")
            .data(null)
            .build();
    return ResponseEntity.ok(apiResponse);
  }
}
