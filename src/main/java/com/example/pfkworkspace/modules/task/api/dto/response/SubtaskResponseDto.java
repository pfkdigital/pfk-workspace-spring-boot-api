package com.example.pfkworkspace.modules.task.api.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SubtaskResponseDto {
    private UUID id;
    private UUID taskId;
    private String title;
    private boolean done;
    private Instant createdAt;
    private Instant updatedAt;
}
