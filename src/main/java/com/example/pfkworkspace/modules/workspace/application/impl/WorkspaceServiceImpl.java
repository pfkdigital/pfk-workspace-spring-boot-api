package com.example.pfkworkspace.modules.workspace.application.impl;

import com.example.pfkworkspace.modules.auth.application.UserContextService;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.workspace.api.dto.*;
import com.example.pfkworkspace.modules.workspace.api.exception.WorkspaceMemberNotFoundException;
import com.example.pfkworkspace.modules.workspace.api.exception.WorkspaceNotFoundException;
import com.example.pfkworkspace.modules.workspace.application.WorkspaceSecurityService;
import com.example.pfkworkspace.modules.workspace.application.WorkspaceService;
import com.example.pfkworkspace.modules.workspace.application.mapper.WorkspaceMapper;
import com.example.pfkworkspace.modules.workspace.domain.UpdateMemberRole;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceMember;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceMemberRepository;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

  private final UserContextService userContextService;
  private final WorkspaceMapper workspaceMapper;
  private final WorkspaceRepository workspaceRepository;
  private final WorkspaceMemberRepository workspaceMemberRepository;
  private final WorkspaceSecurityService workspaceSecurityService;

  @Override
  @Transactional
  public CreateWorkspaceResponseDto createWorkspace(
      CreateWorkspaceRequestDto createWorkspaceRequestDto) {
    User owner = userContextService.getCurrentUser();

    Workspace newWorkspace =
        workspaceRepository.save(
            Workspace.builder()
                .name(createWorkspaceRequestDto.name())
                .description(createWorkspaceRequestDto.description())
                .imageUrl(createWorkspaceRequestDto.imageUrl())
                .owner(owner)
                .build());

    WorkspaceMember workspaceMember =
        WorkspaceMember.builder()
            .user(owner)
            .role(WorkspaceRole.OWNER)
            .joinedAt(Instant.now())
            .build();

    newWorkspace.addWorkspaceMember(workspaceMember);

    log.info(
        "Workspace created: id={}, name={}, ownerId={}",
        newWorkspace.getId(),
        newWorkspace.getName(),
        owner.getId());

    return CreateWorkspaceResponseDto.builder()
        .id(newWorkspace.getId())
        .name(newWorkspace.getName())
        .description(newWorkspace.getDescription())
        .imageUrl(newWorkspace.getImageUrl())
        .createdAt(newWorkspace.getCreatedAt())
        .updatedAt(newWorkspace.getUpdatedAt())
        .build();
  }

  @Override
  @Transactional
  public UpdateWorkspaceResponseDto updateWorkspace(
      UUID workspaceId, UpdateWorkspaceRequestDto updateWorkspaceRequestDto) {
    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(
                () ->
                    new WorkspaceNotFoundException(
                        "Workspace not found with this id " + workspaceId));

    boolean isOwner = workspaceSecurityService.isOwner(workspaceId);
    if (!isOwner) {
      throw new AuthorizationDeniedException(
          "Only the owner of the current workspace can edit the workspace");
    }

    workspace.setName(updateWorkspaceRequestDto.name());
    workspace.setDescription(updateWorkspaceRequestDto.description());
    workspace.setImageUrl(updateWorkspaceRequestDto.imageUrl());

    Workspace updatedWorkSpace = workspaceRepository.save(workspace);

    log.info("Workspace updated: id={}", workspaceId);

    return UpdateWorkspaceResponseDto.builder()
        .name(updateWorkspaceRequestDto.name())
        .description(updateWorkspaceRequestDto.description())
        .imageUrl(updateWorkspaceRequestDto.imageUrl())
        .updatedAt(updatedWorkSpace.getUpdatedAt())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<WorkspaceSummaryDto> getWorkspaces() {
    User currentUser = userContextService.getCurrentUser();
    List<Workspace> memberShips =
        workspaceMemberRepository.findWorkspacesByUserId(currentUser.getId());

    return memberShips.stream().map(workspaceMapper::toSummaryDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public WorkspaceDetailDto getWorkspaceDetail(UUID workspaceId) {

    Workspace workspace =
        workspaceRepository
            .findByIdWithDetails(workspaceId)
            .orElseThrow(
                () ->
                    new WorkspaceNotFoundException("Workspace not found, with ID: " + workspaceId));

    if (!workspaceSecurityService.isMember(workspaceId)) {
      throw new AuthorizationDeniedException("Not a member of this workspace");
    }

    return workspaceMapper.toDetailDto(workspace);
  }

  @Override
  @Transactional
  public void deleteWorkspace(UUID workspaceId) {
    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(
                () ->
                    new WorkspaceNotFoundException("Workspace not found with id: " + workspaceId));

    boolean isOwner = workspaceSecurityService.isOwner(workspaceId);
    if (!isOwner) {
      throw new AuthorizationDeniedException(
          "Only the owner of a workspace can delete the workspace");
    }

    workspaceRepository.delete(workspace);

    log.info("Workspace deleted: id={}", workspaceId);
  }

  @Override
  @Transactional
  public void removeUserFromWorkspace(UUID workspaceId, UUID userId) {
    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(
                () ->
                    new WorkspaceNotFoundException("Workspace not found with id: " + workspaceId));

    if (!workspaceSecurityService.isOwnerOrAdmin(workspaceId)) {
      throw new AuthorizationDeniedException(
          "Only owners and admins are allowed to remove users from a workspace");
    }

    WorkspaceMember workspaceMember =
        workspaceMemberRepository
            .findByUserIdAndWorkspaceId(userId, workspaceId)
            .orElseThrow(
                () ->
                    new WorkspaceMemberNotFoundException(
                        "Workspace Member was not found with the id: " + userId));

    workspace.removeWorkspaceMember(workspaceMember);
    workspaceRepository.save(workspace);

    log.info("Member userId={} removed from workspaceId={}", userId, workspaceId);
  }

  @Override
  @Transactional
  public void updateMemberRole(UUID workspaceId, UUID userId, UpdateMemberRole role) {
    User currentUser = userContextService.getCurrentUser();

    WorkspaceMember currentMember =
        workspaceMemberRepository
            .findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId)
            .orElseThrow(
                () -> new AuthorizationDeniedException("You are not a member of this workspace"));

    if (currentMember.getRole() == WorkspaceRole.MEMBER) {
      throw new AuthorizationDeniedException(
          "Only owners and admins are allowed to update user roles in a workspace");
    }

    WorkspaceMember targetMember =
        workspaceMemberRepository
            .findByUserIdAndWorkspaceId(userId, workspaceId)
            .orElseThrow(
                () ->
                    new WorkspaceMemberNotFoundException(
                        "Workspace member not found with user id " + userId));

    WorkspaceRole workspaceRole =
        switch (role) {
          case ADMIN -> WorkspaceRole.ADMIN;
          case MEMBER -> WorkspaceRole.MEMBER;
        };

    targetMember.setRole(workspaceRole);
    workspaceMemberRepository.save(targetMember);

    log.info("Member userId={} role updated to {} in workspaceId={}", userId, role, workspaceId);
  }

  @Override
  @Transactional
  public void transferOwnerShip(UUID workspaceId, UUID userId) {
    User currentUser = userContextService.getCurrentUser();
    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(
                () ->
                    new WorkspaceNotFoundException(
                        "Workspace was not found workspaceId: " + workspaceId));

    if (!currentUser.getId().equals(workspace.getOwner().getId())) {
      throw new AuthorizationDeniedException("Only the workspace owner can transfer ownership");
    }

    WorkspaceMember newOwnerMember =
        workspaceMemberRepository
            .findByUserIdAndWorkspaceId(userId, workspaceId)
            .orElseThrow(
                () ->
                    new WorkspaceMemberNotFoundException("User is not a member of this workspace"));

    WorkspaceMember currentOwnerMember =
        workspaceMemberRepository
            .findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId)
            .orElseThrow(
                () -> new WorkspaceMemberNotFoundException("Current owner membership not found"));

    workspace.setOwner(newOwnerMember.getUser());
    newOwnerMember.setRole(WorkspaceRole.OWNER);
    currentOwnerMember.setRole(WorkspaceRole.ADMIN);

    workspaceRepository.save(workspace);

    log.info(
        "Ownership of workspaceId={} transferred from userId={} to userId={}",
        workspaceId,
        currentUser.getId(),
        userId);
  }

  // TODO
  // When the Task and other modules are in place
  /*
   * Leave Workspace
   * User leaves workspace by pressing button on frontend - need userId + Workspace Id
   * Removes workspace member
   * All owned tasks need to be reassigned to owner
   * All attachments need to be reassigned to owner
   * */
  @Override
  @Transactional
  public void leaveWorkspace(UUID workspaceId) {
    throw new UnsupportedOperationException("Operation leave workspace is not supported");
  }
}
