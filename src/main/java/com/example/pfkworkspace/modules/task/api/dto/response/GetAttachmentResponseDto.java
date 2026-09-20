package com.example.pfkworkspace.modules.task.api.dto.response;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class GetAttachmentResponseDto {
    private String presignedUrl;
    private String fileName;
    private String contentType;
    private long attachmentSize;
    private UUID attachmentId;
}
