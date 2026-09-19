package com.example.pfkworkspace.modules.task.application;

import com.example.pfkworkspace.modules.task.api.dto.request.CreateAttachmentRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.CreateAttachmentResponseDto;
import com.example.pfkworkspace.modules.task.application.messaging.ScanResultMessage;

import java.util.UUID;

public interface AttachmentService {
    CreateAttachmentResponseDto createAttachment(UUID workspaceId, UUID projectId, UUID taskId,CreateAttachmentRequestDto request);
    void applyScanResult(ScanResultMessage scanResult);
}
