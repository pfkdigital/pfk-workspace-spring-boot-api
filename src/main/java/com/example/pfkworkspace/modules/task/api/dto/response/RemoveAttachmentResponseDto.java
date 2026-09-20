package com.example.pfkworkspace.modules.task.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RemoveAttachmentResponseDto {
    private UUID attachmentId;
    private UUID taskId;
}
