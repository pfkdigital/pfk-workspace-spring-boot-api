package com.example.pfkworkspace.modules.task.api;

import com.example.pfkworkspace.common.api.ApiResponse;
import com.example.pfkworkspace.modules.task.api.dto.request.CreateAttachmentRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.CreateAttachmentResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.GetAttachmentResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.RemoveAttachmentResponseDto;
import com.example.pfkworkspace.modules.task.application.AttachmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/{workspaceId}/{projectId}/{taskId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

  private final AttachmentService attachmentService;

  @PostMapping
  public ResponseEntity<ApiResponse> createAttachment(
      @PathVariable UUID workspaceId,
      @PathVariable UUID projectId,
      @PathVariable UUID taskId,
      @Valid @RequestBody CreateAttachmentRequestDto requestDto) {
    CreateAttachmentResponseDto responseDto =
        attachmentService.createAttachment(workspaceId, projectId, taskId, requestDto);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .data(responseDto)
            .success(true)
            .message("Attachment creation initialized")
            .errors(null)
            .timestamp(Instant.now())
            .build();
    return ResponseEntity.ok(apiResponse);
  }

  @GetMapping("/{attachmentId}")
  public ResponseEntity<ApiResponse> getAttachment(
      @PathVariable UUID workspaceId,
      @PathVariable UUID projectId,
      @PathVariable UUID taskId,
      @PathVariable UUID attachmentId) {
    GetAttachmentResponseDto responseDto =
        attachmentService.getAttachment(workspaceId, projectId, taskId, attachmentId);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .data(responseDto)
            .success(true)
            .message("Attachment retrieval initialized")
            .errors(null)
            .timestamp(Instant.now())
            .build();
    return ResponseEntity.ok(apiResponse);
  }

  @DeleteMapping("/{attachmentId}")
  public ResponseEntity<ApiResponse> deleteAttachment(
      @PathVariable UUID workspaceId,
      @PathVariable UUID projectId,
      @PathVariable UUID taskId,
      @PathVariable UUID attachmentId) {
    RemoveAttachmentResponseDto responseDto =
        attachmentService.deleteAttachment(workspaceId, projectId, taskId, attachmentId);
    ApiResponse apiResponse =
        ApiResponse.builder()
            .data(responseDto)
            .success(true)
            .message("Attachment deletion initialized")
            .errors(null)
            .timestamp(Instant.now())
            .build();
    return ResponseEntity.ok(apiResponse);
  }
}
