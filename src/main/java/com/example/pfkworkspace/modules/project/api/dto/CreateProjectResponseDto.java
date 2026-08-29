package com.example.pfkworkspace.modules.project.api.dto;

import com.example.pfkworkspace.modules.project.domain.ProjectStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateProjectResponseDto {
    private UUID id;
    private String name;
    private String description;
    private String color;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate targetDate;
    private Instant createdAt;
    private Instant updatedAt;
}
