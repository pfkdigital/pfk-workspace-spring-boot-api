package com.example.pfkworkspace.modules.task.api;

import com.example.pfkworkspace.common.api.ApiResponse;
import com.example.pfkworkspace.modules.task.api.dto.request.*;
import com.example.pfkworkspace.modules.task.api.dto.response.*;
import com.example.pfkworkspace.modules.task.application.CommentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class CommentController {
  private final CommentService commentService;

  @PutMapping("/{workspaceId}/projects/{projectId}/tasks/{taskId}/comments")
  public ResponseEntity<ApiResponse> addComment(
      @PathVariable UUID workspaceId,
      @PathVariable UUID projectId,
      @PathVariable UUID taskId,
      @Valid @RequestBody AddCommentRequestDto requestDto) {
    AddCommentResponseDto responseDto =
        commentService.addCommentToTask(workspaceId, projectId, taskId, requestDto);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(responseDto)
            .message("Comment added successfully")
            .build();

    return ResponseEntity.ok(apiResponse);
  }

  @PutMapping("/{workspaceId}/projects/{projectId}/tasks/{taskId}/comments/{commentId}")
  public ResponseEntity<ApiResponse> removeComment(
      @PathVariable UUID workspaceId,
      @PathVariable UUID projectId,
      @PathVariable UUID taskId,
      @PathVariable UUID commentId) {
    RemoveCommentResponseDto responseDto =
        commentService.removeCommentFromTask(workspaceId, projectId, taskId, commentId);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .data(responseDto)
            .message("Comment removed successfully")
            .build();

    return ResponseEntity.ok(apiResponse);
  }
}
