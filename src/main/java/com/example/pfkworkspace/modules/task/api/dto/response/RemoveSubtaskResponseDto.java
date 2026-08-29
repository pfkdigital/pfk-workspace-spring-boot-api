package com.example.pfkworkspace.modules.task.api.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RemoveSubtaskResponseDto {
    private UUID id;
    private UUID taskId;
}
