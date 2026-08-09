package com.example.pfkworkspace.modules.workspace.application;

import com.example.pfkworkspace.modules.workspace.api.dto.*;
import com.example.pfkworkspace.modules.workspace.domain.UpdateMemberRole;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;

import java.util.List;
import java.util.UUID;

public interface WorkspaceService {
  CreateWorkspaceResponseDto createWorkspace(CreateWorkspaceRequestDto createWorkspaceRequestDto);
  UpdateWorkspaceResponseDto updateWorkspace(UUID workspaceId, UpdateWorkspaceRequestDto updateWorkspaceRequestDto);
  List<WorkspaceSummaryDto> getWorkspaces();
  WorkspaceDetailDto getWorkspaceDetail(UUID workspaceId);
  void deleteWorkspace(UUID workspaceId);
  void removeUserFromWorkspace(UUID workspaceId, UUID userId);
  void updateMemberRole(UUID workspaceId, UUID userId, UpdateMemberRole role);
  void transferOwnerShip(UUID workspaceId, UUID userId);
  void leaveWorkspace(UUID workspaceId);
}
