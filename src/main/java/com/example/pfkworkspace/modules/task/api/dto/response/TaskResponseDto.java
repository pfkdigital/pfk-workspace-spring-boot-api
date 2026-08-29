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
public class TaskResponseDto {
    private UUID id;
    private UUID projectId;
    private String title;
    private TaskStatus status;
    private TaskPriority priority;
    private AssigneeDto assignee;
    private LocalDate dueDate;
    private List<LabelDto> labels;
    private int subtaskCount;
    private int commentCount;
    private int attachmentCount;
    private Instant createdAt;

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
