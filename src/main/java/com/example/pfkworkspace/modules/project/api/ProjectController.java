package com.example.pfkworkspace.modules.project.api;

import com.example.pfkworkspace.common.api.ApiResponse;
import com.example.pfkworkspace.modules.project.api.dto.*;
import com.example.pfkworkspace.modules.project.application.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class ProjectController {
  private final ProjectService projectService;

  @PostMapping("/{workspaceId}/projects")
  public ResponseEntity<ApiResponse> createProject(
      @PathVariable UUID workspaceId,
      @Valid @RequestBody CreateProjectDtoRequest createProjectDtoRequest) {
    CreateProjectResponseDto responseDto =
        projectService.createProject(workspaceId, createProjectDtoRequest);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(responseDto)
            .message(HttpStatus.CREATED.name())
            .build();

    return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
  }

  @GetMapping("/{workspaceId}/projects")
  public ResponseEntity<ApiResponse> getProjects(@PathVariable UUID workspaceId) {
    List<ProjectResponseDto> projects = projectService.getProjects(workspaceId);
    ApiResponse apiResponse =
        ApiResponse.builder().success(true).data(projects).message(HttpStatus.OK.name()).build();

    return ResponseEntity.ok(apiResponse);
  }

  @GetMapping("/{workspaceId}/projects/{projectId}")
  public ResponseEntity<ApiResponse> getProjectDetail(
      @PathVariable UUID workspaceId, @PathVariable UUID projectId) {
    ProjectDetailResponseDto projectDetail = projectService.getProjectDetail(workspaceId, projectId);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(projectDetail)
            .message(HttpStatus.OK.name())
            .build();

    return ResponseEntity.ok(apiResponse);
  }

  @PutMapping("/{workspaceId}/projects/{projectId}")
  public ResponseEntity<ApiResponse> updateProject(
      @PathVariable UUID workspaceId,
      @PathVariable UUID projectId,
      @Valid @RequestBody UpdateProjectRequestDto updateProjectRequestDto) {
    UpdateProjectResponseDto responseDto =
        projectService.updateProject(workspaceId, projectId, updateProjectRequestDto);
    ApiResponse apiResponse =
        ApiResponse.builder().success(true).data(responseDto).message(HttpStatus.OK.name()).build();

    return ResponseEntity.ok(apiResponse);
  }

  @DeleteMapping("/{workspaceId}/projects/{projectId}")
  public ResponseEntity<ApiResponse> deleteProject(
      @PathVariable UUID workspaceId, @PathVariable UUID projectId) {
    projectService.deleteProject(workspaceId, projectId);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(null)
            .message("Project deleted successfully")
            .build();

    return ResponseEntity.ok(apiResponse);
  }

  @PostMapping("/{workspaceId}/projects/{projectId}/archive")
  public ResponseEntity<ApiResponse> archiveProject(
      @PathVariable UUID workspaceId, @PathVariable UUID projectId) {
    ArchiveProjectResponseDto responseDto = projectService.archiveProject(workspaceId, projectId);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(responseDto)
            .message("Project archived successfully")
            .build();

    return ResponseEntity.ok(apiResponse);
  }
}
