package com.example.pfkworkspace.modules.workspace.application;

import com.example.pfkworkspace.modules.workspace.api.dto.CreateWorkspaceRequestDto;
import com.example.pfkworkspace.modules.workspace.api.dto.CreateWorkspaceResponseDto;

public interface WorkspaceService {
  CreateWorkspaceResponseDto createWorkspace(CreateWorkspaceRequestDto createWorkspaceRequestDto);
}
