package com.example.pfkworkspace.modules.task.api;

import com.example.pfkworkspace.common.api.ApiResponse;
import com.example.pfkworkspace.modules.task.api.dto.request.*;
import com.example.pfkworkspace.modules.task.api.dto.response.*;
import com.example.pfkworkspace.modules.task.application.SubtaskService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class SubtaskController {
  private final SubtaskService subtaskService;

  @PutMapping("/{workspaceId}/projects/{projectId}/tasks/{taskId}/subtasks")
  public ResponseEntity<ApiResponse> addSubtasks(
      @PathVariable UUID workspaceId,
      @PathVariable UUID projectId,
      @PathVariable UUID taskId,
      @Valid @RequestBody AddSubtaskRequestDto requestDto) {
    SubtaskResponseDto responseDto =
        subtaskService.addSubtask(workspaceId, projectId, taskId, requestDto);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(responseDto)
            .message("Subtask added successfully")
            .build();
    return ResponseEntity.ok(apiResponse);
  }

  @DeleteMapping("/{workspaceId}/projects/{projectId}/tasks/{taskId}/subtasks/{subTaskId}")
  public ResponseEntity<ApiResponse> removeSubtasks(
      @PathVariable UUID workspaceId,
      @PathVariable UUID projectId,
      @PathVariable UUID taskId,
      @PathVariable UUID subTaskId) {
    RemoveSubtaskResponseDto responseDto =
        subtaskService.removeSubtask(workspaceId, projectId, taskId, subTaskId);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(responseDto)
            .message("Subtask removed successfully")
            .build();
    return ResponseEntity.ok(apiResponse);
  }

  @PatchMapping("/{workspaceId}/projects/{projectId}/tasks/{taskId}/subtasks/{subTaskId}")
  public ResponseEntity<ApiResponse> updateSubTask(
      @PathVariable UUID workspaceId,
      @PathVariable UUID projectId,
      @PathVariable UUID taskId,
      @PathVariable UUID subTaskId,
      @Valid @RequestBody UpdateSubtaskRequestDto requestDto) {
    SubtaskResponseDto responseDto =
        subtaskService.updateSubtask(workspaceId, projectId, taskId, subTaskId, requestDto);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(responseDto)
            .message("Subtask updated successfully")
            .build();
    return ResponseEntity.ok(apiResponse);
  }
}
