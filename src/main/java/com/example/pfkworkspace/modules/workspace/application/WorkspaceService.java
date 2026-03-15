package com.example.pfkworkspace.modules.workspace.application;

import com.example.pfkworkspace.modules.workspace.api.dto.*;

import java.util.List;
import java.util.UUID;

public interface WorkspaceService {
  CreateWorkspaceResponseDto createWorkspace(CreateWorkspaceRequestDto createWorkspaceRequestDto);
  UpdateWorkspaceResponseDto updateWorkspace(UUID workspaceId, UpdateWorkspaceRequestDto updateWorkspaceRequestDto);
  List<WorkspaceSummaryDto> getWorkspaces();
  WorkspaceDetailDto getWorkspaceDetail(UUID workspaceId);
  void deleteWorkspace(UUID workspaceId);
}
