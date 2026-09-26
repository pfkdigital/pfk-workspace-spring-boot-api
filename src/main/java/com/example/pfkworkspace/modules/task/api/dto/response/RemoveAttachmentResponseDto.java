package com.example.pfkworkspace.modules.task.api.dto.response;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RemoveAttachmentResponseDto {
    private UUID attachmentId;
    private UUID taskId;
}
