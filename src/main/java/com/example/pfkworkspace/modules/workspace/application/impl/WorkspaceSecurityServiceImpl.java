package com.example.pfkworkspace.modules.workspace.application.impl;

import com.example.pfkworkspace.modules.auth.application.UserContextService;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.workspace.application.WorkspaceSecurityService;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceSecurityServiceImpl implements WorkspaceSecurityService {

  private final UserContextService userContextService;
  private final WorkspaceMemberRepository workspaceMemberRepository;

  @Override
  public boolean isOwner(UUID workspaceId) {
    return hasRole(workspaceId, WorkspaceRole.OWNER);
  }

  @Override
  public boolean isAdmin(UUID workspaceId) {
    return hasRole(workspaceId, WorkspaceRole.ADMIN);
  }

  @Override
  public boolean isMember(UUID workspaceId) {
    User currentUser = userContextService.getCurrentUser();
    return workspaceMemberRepository
        .findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId)
        .isPresent();
  }

  @Override
  public boolean isOwnerOrAdmin(UUID workspaceId) {
    User currentUser = userContextService.getCurrentUser();

    return workspaceMemberRepository
        .findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId)
        .map(
            member ->
                member.getRole().equals(WorkspaceRole.OWNER)
                    || member.getRole().equals(WorkspaceRole.ADMIN))
        .orElse(false);
  }

  @Override
  public boolean hasRole(UUID workspaceId, WorkspaceRole requiredRole) {
    User currentUser = userContextService.getCurrentUser();
    return workspaceMemberRepository
        .findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId)
        .map(member -> requiredRole.equals(member.getRole()))
        .orElse(false);
  }
}
