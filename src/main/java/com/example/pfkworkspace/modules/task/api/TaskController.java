package com.example.pfkworkspace.modules.task.api;

import com.example.pfkworkspace.common.api.ApiResponse;
import com.example.pfkworkspace.common.api.PageResponse;
import com.example.pfkworkspace.modules.task.api.dto.request.CreateTaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateTaskAssigneeRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateTaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateTaskStatusRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.CreateTaskResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.TaskDetailResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.TaskResponseDto;
import com.example.pfkworkspace.modules.task.application.TaskService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class TaskController {
  private final TaskService taskService;

  @PostMapping("/{workspaceId}/projects/{projectId}/tasks")
  public ResponseEntity<ApiResponse> createTask(
      @PathVariable UUID workspaceId,
      @PathVariable UUID projectId,
      @Valid @RequestBody CreateTaskRequestDto requestDto) {
    CreateTaskResponseDto responseDto = taskService.createTask(requestDto, workspaceId, projectId);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(responseDto)
            .message(HttpStatus.CREATED.name())
            .build();

    return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
  }

  @GetMapping("/{workspaceId}/projects/{projectId}/tasks")
  public ResponseEntity<PageResponse> getTasks(
      @PathVariable UUID workspaceId, @PathVariable UUID projectId, Pageable pageable) {
    Page<TaskResponseDto> tasks = taskService.getTasks(workspaceId, projectId, pageable);
    PageResponse pageResponse =
        PageResponse.builder()
            .success(true)
            .message(HttpStatus.OK.name())
            .data(tasks.getContent())
            .page(tasks.getNumber())
            .size(tasks.getSize())
            .totalElements(tasks.getTotalElements())
            .totalPages(tasks.getTotalPages())
            .build();

    return ResponseEntity.ok(pageResponse);
  }

  @GetMapping("/{workspaceId}/projects/{projectId}/tasks/{taskId}")
  public ResponseEntity<ApiResponse> getTaskDetail(
      @PathVariable UUID workspaceId, @PathVariable UUID projectId, @PathVariable UUID taskId) {
    TaskDetailResponseDto responseDto = taskService.getTaskDetail(workspaceId, projectId, taskId);
    ApiResponse apiResponse =
        ApiResponse.builder().success(true).data(responseDto).message(HttpStatus.OK.name()).build();

    return ResponseEntity.ok(apiResponse);
  }

  @PatchMapping("/{workspaceId}/projects/{projectId}/tasks/{taskId}/status")
  public ResponseEntity<ApiResponse> updateTaskStatus(
      @PathVariable UUID workspaceId,
      @PathVariable UUID projectId,
      @PathVariable UUID taskId,
      @Valid @RequestBody UpdateTaskStatusRequestDto requestDto) {
    TaskDetailResponseDto responseDto =
        taskService.updateTaskStatus(workspaceId, projectId, taskId, requestDto);
    ApiResponse apiResponse =
        ApiResponse.builder().success(true).data(responseDto).message(HttpStatus.OK.name()).build();

    return ResponseEntity.ok(apiResponse);
  }

  @PatchMapping("/{workspaceId}/projects/{projectId}/tasks/{taskId}/assignee")
  public ResponseEntity<ApiResponse> updateTaskAssignee(
      @PathVariable UUID workspaceId,
      @PathVariable UUID projectId,
      @PathVariable UUID taskId,
      @Valid @RequestBody UpdateTaskAssigneeRequestDto requestDto) {
    TaskDetailResponseDto responseDto =
        taskService.updateTaskAssignee(workspaceId, projectId, taskId, requestDto);
    ApiResponse apiResponse =
        ApiResponse.builder().success(true).data(responseDto).message(HttpStatus.OK.name()).build();

    return ResponseEntity.ok(apiResponse);
  }

  @PatchMapping("/{workspaceId}/projects/{projectId}/tasks/{taskId}")
  public ResponseEntity<ApiResponse> updateTask(
      @PathVariable UUID workspaceId,
      @PathVariable UUID projectId,
      @PathVariable UUID taskId,
      @Valid @RequestBody UpdateTaskRequestDto requestDto) {
    TaskDetailResponseDto responseDto =
        taskService.updateTask(workspaceId, projectId, taskId, requestDto);
    ApiResponse apiResponse =
        ApiResponse.builder().success(true).data(responseDto).message(HttpStatus.OK.name()).build();

    return ResponseEntity.ok(apiResponse);
  }

  @DeleteMapping("/{workspaceId}/projects/{projectId}/tasks/{taskId}")
  public ResponseEntity<ApiResponse> deleteTask(
      @PathVariable UUID workspaceId, @PathVariable UUID projectId, @PathVariable UUID taskId) {
    taskService.deleteTask(workspaceId, projectId, taskId);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(null)
            .message("Task deleted successfully")
            .build();

    return ResponseEntity.ok(apiResponse);
  }
}
