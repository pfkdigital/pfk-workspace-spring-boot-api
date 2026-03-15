package com.example.pfkworkspace.modules.workspace.application;

import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;

import java.util.UUID;

public interface WorkspaceSecurityService {
    boolean isOwner(UUID workspaceId);
    boolean isMember(UUID workspaceId);
    boolean hasRole(UUID workspaceId, WorkspaceRole requiredRole);
}
