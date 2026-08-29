package com.example.pfkworkspace.modules.task.api.dto.response;

import com.example.pfkworkspace.modules.task.domain.TaskPriority;
import com.example.pfkworkspace.modules.task.domain.TaskStatus;
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
public class CreateTaskResponseDto {
    private UUID id;
    private UUID projectId;
    private UUID workspaceId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private AssigneeDto assignee;
    private LocalDate dueDate;
    private Instant completedAt;
    private List<LabelDto> labels;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AssigneeDto {
        private UUID id;
        private String username;
        private String firstName;
        private String lastName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LabelDto {
        private UUID id;
        private String name;
        private String color;
    }
}
