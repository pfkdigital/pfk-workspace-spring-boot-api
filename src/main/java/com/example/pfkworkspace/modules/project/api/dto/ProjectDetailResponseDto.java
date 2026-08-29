package com.example.pfkworkspace.modules.project.api.dto;

import com.example.pfkworkspace.modules.project.domain.ProjectStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProjectDetailResponseDto {
    private UUID id;
    private UUID workspaceId;
    private String name;
    private String description;
    private String color;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate targetDate;
    private Instant archivedAt;
    private CreatedByDto createdBy;
    private List<ProjectLinkDto> links;
    private ProjectStatsDto stats;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreatedByDto {
        private UUID id;
        private String username;
        private String firstName;
        private String lastName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProjectStatsDto {
        private int progress;
        private int todoCount;
        private int inProgressCount;
        private int inReviewCount;
        private int doneCount;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProjectLinkDto {
        private UUID id;
        private String label;
        private String url;
        private String icon;
    }
}
