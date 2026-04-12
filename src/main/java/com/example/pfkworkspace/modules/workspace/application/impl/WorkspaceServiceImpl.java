package com.example.pfkworkspace.modules.workspace.application.impl;

import com.example.pfkworkspace.modules.auth.application.UserContextService;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.user.infrastructure.repo.UserRepository;
import com.example.pfkworkspace.modules.workspace.api.dto.*;
import com.example.pfkworkspace.modules.workspace.api.exception.WorkspaceMemberNotFoundException;
import com.example.pfkworkspace.modules.workspace.api.exception.WorkspaceNotFoundException;
import com.example.pfkworkspace.modules.workspace.application.WorkspaceSecurityService;
import com.example.pfkworkspace.modules.workspace.application.WorkspaceService;
import com.example.pfkworkspace.modules.workspace.application.mapper.WorkspaceMapper;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceMember;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceMemberRepository;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
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

  private final UserRepository userRepository;
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
                .imageUrl(null)
                .owner(owner)
                .build());

    WorkspaceMember workspaceMember =
        WorkspaceMember.builder()
            .user(owner)
            .role(WorkspaceRole.OWNER)
            .joinedAt(Instant.now())
            .build();

    newWorkspace.addWorkspaceMember(workspaceMember);

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
    boolean isOwner = workspaceSecurityService.isOwner(workspaceId);
    if (!isOwner) {
      throw new AuthorizationDeniedException(
          "Only the owner of the current workspace can edit the workspace");
    }

    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(
                () ->
                    new WorkspaceNotFoundException(
                        "Workspace not found with this id " + workspaceId));

    workspace.setName(updateWorkspaceRequestDto.name());
    workspace.setDescription(updateWorkspaceRequestDto.description());
    workspace.setImageUrl(updateWorkspaceRequestDto.imageUrl());

    Workspace updatedWorkSpace = workspaceRepository.save(workspace);

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

    if (!workspaceSecurityService.isMember(workspaceId)) {
      throw new AuthorizationDeniedException("Not a member of this workspace");
    }

    Workspace workspace =
        workspaceRepository
            .findByIdWithDetails(workspaceId)
            .orElseThrow(
                () ->
                    new WorkspaceNotFoundException("Workspace not found, with ID: " + workspaceId));

    return workspaceMapper.toDetailDto(workspace);
  }

  @Override
  @Transactional
  public void deleteWorkspace(UUID workspaceId) {
    boolean isOwner = workspaceSecurityService.isOwner(workspaceId);
    if(!isOwner) {
      throw new AccessDeniedException("Only the owner of a workspace can delete the workspace");
    }

    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found with id: " + workspaceId));

    workspaceRepository.delete(workspace);
  }

  @Override
  @Transactional
  public void removeUserFromWorkspace(UUID workspaceId, UUID userId) {
    if(!workspaceSecurityService.isOwnerOrAdmin(workspaceId)) {
      throw new AuthorizationDeniedException("Only owners and admins are allowed to remove users from a workspace");
    }

    Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found with id: " + workspaceId));
    WorkspaceMember workspaceMember = workspaceMemberRepository.findById(userId).orElseThrow(() -> new WorkspaceMemberNotFoundException("Workspace Member was not found with the id" + userId));

    workspace.removeWorkspaceMember(workspaceMember);
    workspaceRepository.save(workspace);

    log.info("Workspace Member was successfully removed from the workspace");
  }

  @Override
  @Transactional
  public void updateMemberRole(UUID workspaceId, UUID userId, WorkspaceRole role) {
    if (!workspaceSecurityService.isOwnerOrAdmin(workspaceId)) {
      throw new AuthorizationDeniedException(
          "Only owners and admins are allowed to update user roles in a workspace");
    }

    if (role == WorkspaceRole.OWNER && !workspaceSecurityService.isOwner(workspaceId)) {
      throw new AuthorizationDeniedException("Only the owner can appoint a new owner");
    }

    WorkspaceMember workspaceMember =
        workspaceMemberRepository
            .findByUserIdAndWorkspaceId(userId, workspaceId)
            .orElseThrow(
                () ->
                    new WorkspaceMemberNotFoundException(
                        "Workspace Member was not found with the user id " + userId));

    workspaceMember.setRole(role);
    workspaceMemberRepository.save(workspaceMember);

    log.info("Workspace Member role was successfully updated to {}", role);
  }
}
