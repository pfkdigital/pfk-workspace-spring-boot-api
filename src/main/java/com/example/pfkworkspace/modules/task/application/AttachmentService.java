package com.example.pfkworkspace.modules.task.application;

import com.example.pfkworkspace.modules.task.api.dto.request.CreateAttachmentRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.CreateAttachmentResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.GetAttachmentResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.RemoveAttachmentResponseDto;
import com.example.pfkworkspace.modules.task.application.messaging.ScanResultMessage;

import java.util.UUID;

public interface AttachmentService {
    CreateAttachmentResponseDto createAttachment(UUID workspaceId, UUID projectId, UUID taskId,CreateAttachmentRequestDto request);
    GetAttachmentResponseDto getAttachment(UUID workspaceId, UUID projectId, UUID taskId, UUID attachmentId);
    RemoveAttachmentResponseDto deleteAttachment(UUID workspaceId, UUID projectId, UUID taskId, UUID attachmentId);
    void applyScanResult(ScanResultMessage scanResult);
}
