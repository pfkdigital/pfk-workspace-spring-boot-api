package com.example.pfkworkspace.modules.workspace.api;

import com.example.pfkworkspace.common.api.ApiResponse;
import com.example.pfkworkspace.modules.workspace.api.dto.*;
import com.example.pfkworkspace.modules.workspace.application.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspace")
@RequiredArgsConstructor
public class WorkspaceController {
  private final WorkspaceService workspaceService;

  @PostMapping
  public ResponseEntity<ApiResponse> createWorkspace(
      @Valid @RequestBody CreateWorkspaceRequestDto createWorkspaceRequestDto) {
    CreateWorkspaceResponseDto responseDto =
        workspaceService.createWorkspace(createWorkspaceRequestDto);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(responseDto)
            .message(HttpStatus.CREATED.name())
            .build();

    return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<ApiResponse> getWorkspaces() {
    List<WorkspaceSummaryDto> workspaces = workspaceService.getWorkspaces();
    ApiResponse apiResponse =
        ApiResponse.builder().success(true).data(workspaces).message(HttpStatus.OK.name()).build();

    return ResponseEntity.ok(apiResponse);
  }

  @GetMapping("/{workspaceId}")
  public ResponseEntity<ApiResponse> getWorkspaceDetail(@PathVariable UUID workspaceId) {
    WorkspaceDetailDto workspaceDetail = workspaceService.getWorkspaceDetail(workspaceId);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(workspaceDetail)
            .message(HttpStatus.OK.name())
            .build();

    return ResponseEntity.ok(apiResponse);
  }

  @PutMapping("/{workspaceId}")
  public ResponseEntity<ApiResponse> updateWorkspace(
      @PathVariable UUID workspaceId,
      @Valid @RequestBody UpdateWorkspaceRequestDto updateWorkspaceRequestDto) {
    UpdateWorkspaceResponseDto responseDto =
        workspaceService.updateWorkspace(workspaceId, updateWorkspaceRequestDto);
    ApiResponse apiResponse =
        ApiResponse.builder().success(true).data(responseDto).message(HttpStatus.OK.name()).build();

    return ResponseEntity.ok(apiResponse);
  }

  @DeleteMapping("/{workspaceId}")
  public ResponseEntity<ApiResponse> deleteWorkspace(@PathVariable UUID workspaceId) {
    workspaceService.deleteWorkspace(workspaceId);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(null)
            .message("Workspace deleted successfully")
            .build();

    return ResponseEntity.ok(apiResponse);
  }
}
