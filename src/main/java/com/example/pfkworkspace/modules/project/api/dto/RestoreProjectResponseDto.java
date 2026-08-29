package com.example.pfkworkspace.modules.project.api.dto;

import com.example.pfkworkspace.modules.project.domain.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RestoreProjectResponseDto {
    private UUID projectId;
    private ProjectStatus status;
}
