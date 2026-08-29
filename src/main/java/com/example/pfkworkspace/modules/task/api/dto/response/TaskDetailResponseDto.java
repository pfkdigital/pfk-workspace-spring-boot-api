package com.example.pfkworkspace.modules.task.api.dto.response;

import com.example.pfkworkspace.modules.task.domain.AttachmentStatus;
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
public class TaskDetailResponseDto {
    private UUID id;
    private UUID projectId;
    private UUID workspaceId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private UserSummaryDto assignee;
    private LocalDate dueDate;
    private Instant completedAt;
    private List<LabelDto> labels;
    private List<SubtaskDto> subtasks;
    private List<AttachmentDto> attachments;
    private List<CommentDto> comments;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserSummaryDto {
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

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubtaskDto {
        private UUID id;
        private String title;
        private boolean done;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AttachmentDto {
        private UUID id;
        private String filename;
        private String contentType;
        private String extension;
        private long sizeBytes;
        private AttachmentStatus status;
        private UserSummaryDto uploadedBy;
        private Instant createdAt;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommentDto {
        private UUID id;
        private String body;
        private UserSummaryDto author;
        private Instant editedAt;
        private Instant createdAt;
    }
}
