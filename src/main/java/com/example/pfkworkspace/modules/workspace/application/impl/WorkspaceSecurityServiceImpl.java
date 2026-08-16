package com.example.pfkworkspace.modules.workspace.application.impl;

import com.example.pfkworkspace.modules.workspace.application.WorkspaceSecurityService;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("workspaceSecurity")
@RequiredArgsConstructor
public class WorkspaceSecurityServiceImpl implements WorkspaceSecurityService {

  private final WorkspaceMembershipRequestCache membershipCache;

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
    return membershipCache.get(workspaceId).isPresent();
  }

  @Override
  public boolean isOwnerOrAdmin(UUID workspaceId) {
    return membershipCache
        .get(workspaceId)
        .map(
            member ->
                member.getRole().equals(WorkspaceRole.OWNER)
                    || member.getRole().equals(WorkspaceRole.ADMIN))
        .orElse(false);
  }

  @Override
  public boolean hasRole(UUID workspaceId, WorkspaceRole requiredRole) {
    return membershipCache
        .get(workspaceId)
        .map(member -> requiredRole.equals(member.getRole()))
        .orElse(false);
  }
}
