package com.example.pfkworkspace.modules.project.application.impl;

import com.example.pfkworkspace.modules.auth.application.UserContextService;
import com.example.pfkworkspace.modules.project.api.dto.*;
import com.example.pfkworkspace.modules.project.api.exception.ProjectNotFoundException;
import com.example.pfkworkspace.modules.project.application.ProjectService;
import com.example.pfkworkspace.modules.project.application.mapper.ProjectMapper;
import com.example.pfkworkspace.modules.project.domain.Project;
import com.example.pfkworkspace.modules.project.domain.ProjectStatus;
import com.example.pfkworkspace.modules.project.infrastructure.repo.ProjectRepository;
import com.example.pfkworkspace.modules.task.infrastructure.repo.TaskRepository;
import com.example.pfkworkspace.modules.workspace.api.exception.WorkspaceNotFoundException;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

  private final TaskRepository taskRepository;
  private final UserContextService userContextService;
  private final WorkspaceRepository workspaceRepository;
  private final ProjectRepository projectRepository;
  private final ProjectMapper projectMapper;

  @Override
  @PreAuthorize("@workspaceSecurity.isMember(#workspaceId)")
  @Transactional
  public CreateProjectResponseDto createProject(
      @P("workspaceId") UUID workspaceId, CreateProjectDtoRequest requestDto) {

    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(
                () ->
                    new WorkspaceNotFoundException("Workspace not found with id: " + workspaceId));

    Project project =
        Project.builder()
            .name(requestDto.name())
            .description(requestDto.description())
            .color(requestDto.color())
            .status(ProjectStatus.ACTIVE)
            .startDate(requestDto.startDate())
            .targetDate(requestDto.targetDate())
            .createdBy(userContextService.getCurrentUser())
            .build();

    workspace.addProject(project);
    Project newProject = projectRepository.save(project);

    return CreateProjectResponseDto.builder()
        .id(newProject.getId())
        .name(newProject.getName())
        .description(newProject.getDescription())
        .color(newProject.getColor())
        .status(newProject.getStatus())
        .startDate(newProject.getStartDate())
        .targetDate(newProject.getTargetDate())
        .createdAt(newProject.getCreatedAt())
        .updatedAt(newProject.getUpdatedAt())
        .build();
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isMember(#workspaceId)")
  @Transactional(readOnly = true)
  public List<ProjectResponseDto> getProjects(@P("workspaceId") UUID workspaceId) {

    return projectRepository.findByWorkspaceId(workspaceId).stream()
        .map(projectMapper::toSummaryDto)
        .toList();
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isMember(#workspaceId)")
  @Transactional(readOnly = true)
  public ProjectDetailResponseDto getProjectDetail(
      @P("workspaceId") UUID workspaceId, UUID projectId) {
    Project selectedProject =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ProjectNotFoundException("Project not found with id: " + projectId));

    if (requireProjectToBelongToWorkspace(selectedProject, workspaceId)) {
      throw new ProjectNotFoundException("Project not found with id: " + projectId);
    }

    List<TaskRepository.TaskStatusCount> statusCounts =
        taskRepository.countByProjectIdGroupByStatus(projectId);

    return projectMapper.toDetailDto(selectedProject, statusCounts);
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
  @Transactional
  public UpdateProjectResponseDto updateProject(
      @P("workspaceId") UUID workspaceId, UUID projectId, UpdateProjectRequestDto requestDto) {
    Project selectedProject =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ProjectNotFoundException("Project not found with id: " + projectId));

    if (requireProjectToBelongToWorkspace(selectedProject, workspaceId)) {
      throw new ProjectNotFoundException("Project not found with id: " + projectId);
    }

    selectedProject.setName(requestDto.name());
    selectedProject.setDescription(requestDto.description());
    selectedProject.setColor(requestDto.color());
    selectedProject.setStatus(requestDto.status());
    selectedProject.setStartDate(requestDto.startDate());
    selectedProject.setTargetDate(requestDto.targetDate());

    Project updatedProject = projectRepository.save(selectedProject);

    return UpdateProjectResponseDto.builder()
        .id(updatedProject.getId())
        .name(updatedProject.getName())
        .description(updatedProject.getDescription())
        .color(updatedProject.getColor())
        .status(updatedProject.getStatus())
        .startDate(updatedProject.getStartDate())
        .targetDate(updatedProject.getTargetDate())
        .updatedAt(updatedProject.getUpdatedAt())
        .build();
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
  @Transactional
  public void deleteProject(@P("workspaceId") UUID workspaceId, UUID projectId) {

    Project selectedProject =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ProjectNotFoundException("Project not found with id: " + projectId));

    if(requireProjectToBelongToWorkspace(selectedProject, workspaceId)) {
      throw new ProjectNotFoundException("Project not found with id: " + projectId);
    }

    projectRepository.delete(selectedProject);
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
  @Transactional
  public ArchiveProjectResponseDto archiveProject(
      @P("workspaceId") UUID workspaceId, UUID projectId) {
    Project selectedProject =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ProjectNotFoundException("Project not found with id: " + projectId));

    if (requireProjectToBelongToWorkspace(selectedProject, workspaceId)) {
      throw new ProjectNotFoundException("Project not found with id: " + projectId);
    }

    selectedProject.setStatus(ProjectStatus.ARCHIVED);
    selectedProject.setArchivedAt(Instant.now());

    Project updatedProject = projectRepository.save(selectedProject);

    return ArchiveProjectResponseDto.builder()
        .id(updatedProject.getId())
        .status(updatedProject.getStatus())
        .archivedAt(updatedProject.getArchivedAt())
        .build();
  }


  private boolean requireProjectToBelongToWorkspace(Project project, UUID workspaceId) {
    return !project.getWorkspace().getId().equals(workspaceId);
  }
}
