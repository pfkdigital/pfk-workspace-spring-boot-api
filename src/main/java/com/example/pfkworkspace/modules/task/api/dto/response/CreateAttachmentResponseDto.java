package com.example.pfkworkspace.modules.task.api.dto.response;

import com.example.pfkworkspace.modules.task.domain.AttachmentStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CreateAttachmentResponseDto {
    private String filename;
    private String preSignedUrl;
    private AttachmentStatus status;
}
