package com.example.pfkworkspace.modules.project.application;

import com.example.pfkworkspace.modules.project.api.dto.*;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    CreateProjectResponseDto createProject(UUID workspaceId, CreateProjectDtoRequest requestDto);
    List<ProjectResponseDto> getProjects(UUID workspaceId);
    ProjectDetailResponseDto getProjectDetail(UUID workspaceId, UUID projectId);
    UpdateProjectResponseDto updateProject(UUID workspaceId, UUID projectId, UpdateProjectRequestDto requestDto);
    void deleteProject(UUID workspaceId, UUID projectId);
    ArchiveProjectResponseDto archiveProject(UUID workspaceId, UUID projectId);
}
