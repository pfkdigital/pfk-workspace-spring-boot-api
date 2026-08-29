package com.example.pfkworkspace.modules.project.application.mapper;

import com.example.pfkworkspace.modules.project.api.dto.ProjectDetailResponseDto;
import com.example.pfkworkspace.modules.project.api.dto.ProjectResponseDto;
import com.example.pfkworkspace.modules.project.domain.Project;
import com.example.pfkworkspace.modules.project.domain.ProjectLink;
import com.example.pfkworkspace.modules.task.infrastructure.repo.TaskRepository.TaskStatusCount;
import com.example.pfkworkspace.modules.user.domain.User;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponseDto toSummaryDto(Project project) {
        return ProjectResponseDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .color(project.getColor())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .targetDate(project.getTargetDate())
                .taskCount(project.getTasks().size())
                .createdAt(project.getCreatedAt())
                .build();
    }

    public ProjectDetailResponseDto toDetailDto(Project project, List<TaskStatusCount> statusCounts) {
        return ProjectDetailResponseDto.builder()
                .id(project.getId())
                .workspaceId(project.getWorkspace().getId())
                .name(project.getName())
                .description(project.getDescription())
                .color(project.getColor())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .targetDate(project.getTargetDate())
                .archivedAt(project.getArchivedAt())
                .createdBy(project.getCreatedBy() == null ? null : toCreatedByDto(project.getCreatedBy()))
                .links(project.getProjectLinks().stream().map(this::toLinkDto).toList())
                .stats(toStatsDto(statusCounts))
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    private ProjectDetailResponseDto.CreatedByDto toCreatedByDto(User user) {
        return ProjectDetailResponseDto.CreatedByDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    private ProjectDetailResponseDto.ProjectLinkDto toLinkDto(ProjectLink projectLink) {
        return ProjectDetailResponseDto.ProjectLinkDto.builder()
                .id(projectLink.getId())
                .label(projectLink.getLabel())
                .url(projectLink.getUrl())
                .icon(projectLink.getIcon())
                .build();
    }

    private ProjectDetailResponseDto.ProjectStatsDto toStatsDto(List<TaskStatusCount> statusCounts) {
        int todoCount = 0;
        int inProgressCount = 0;
        int inReviewCount = 0;
        int doneCount = 0;

        for (TaskStatusCount statusCount : statusCounts) {
            switch (statusCount.getStatus()) {
                case TODO -> todoCount = (int) statusCount.getCount();
                case IN_PROGRESS -> inProgressCount = (int) statusCount.getCount();
                case REVIEW -> inReviewCount = (int) statusCount.getCount();
                case DONE -> doneCount = (int) statusCount.getCount();
            }
        }

        int total = todoCount + inProgressCount + inReviewCount + doneCount;
        int progress = total == 0 ? 0 : (int) Math.round(doneCount * 100.0 / total);

        return ProjectDetailResponseDto.ProjectStatsDto.builder()
                .progress(progress)
                .todoCount(todoCount)
                .inProgressCount(inProgressCount)
                .inReviewCount(inReviewCount)
                .doneCount(doneCount)
                .build();
    }
}
