package com.example.pfkworkspace.modules.task.application.mapper;

import com.example.pfkworkspace.modules.label.domain.Label;
import com.example.pfkworkspace.modules.task.api.dto.response.AddCommentResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.CreateTaskResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.SubtaskResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.TaskDetailResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.TaskResponseDto;
import com.example.pfkworkspace.modules.task.domain.Attachment;
import com.example.pfkworkspace.modules.task.domain.Comment;
import com.example.pfkworkspace.modules.task.domain.Subtask;
import com.example.pfkworkspace.modules.task.domain.Task;
import com.example.pfkworkspace.modules.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public CreateTaskResponseDto toCreateResponseDto(Task task) {
        return CreateTaskResponseDto.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .workspaceId(task.getWorkspace().getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .assignee(task.getAssignee() == null ? null : toAssigneeDto(task.getAssignee()))
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())
                .labels(task.getLabels().stream().map(this::toLabelDto).toList())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private CreateTaskResponseDto.AssigneeDto toAssigneeDto(User user) {
        return CreateTaskResponseDto.AssigneeDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    private CreateTaskResponseDto.LabelDto toLabelDto(Label label) {
        return CreateTaskResponseDto.LabelDto.builder()
                .id(label.getId())
                .name(label.getName())
                .color(label.getColor())
                .build();
    }

    public Page<TaskResponseDto> toResponsePage(Page<Task> tasks) {
        return tasks.map(this::toResponseDto);
    }

    public TaskResponseDto toResponseDto(Task task) {
        return TaskResponseDto.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .title(task.getTitle())
                .status(task.getStatus())
                .priority(task.getPriority())
                .assignee(task.getAssignee() == null ? null : toSummaryAssigneeDto(task.getAssignee()))
                .dueDate(task.getDueDate())
                .labels(task.getLabels().stream().map(this::toSummaryLabelDto).toList())
                .subtaskCount(task.getSubtasks().size())
                .commentCount(task.getComments().size())
                .attachmentCount(task.getAttachments().size())
                .createdAt(task.getCreatedAt())
                .build();
    }

    private TaskResponseDto.AssigneeDto toSummaryAssigneeDto(User user) {
        return TaskResponseDto.AssigneeDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    private TaskResponseDto.LabelDto toSummaryLabelDto(Label label) {
        return TaskResponseDto.LabelDto.builder()
                .id(label.getId())
                .name(label.getName())
                .color(label.getColor())
                .build();
    }

    public TaskDetailResponseDto toDetailDto(Task task) {
        return TaskDetailResponseDto.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .workspaceId(task.getWorkspace().getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .assignee(task.getAssignee() == null ? null : toUserSummaryDto(task.getAssignee()))
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())
                .labels(task.getLabels().stream().map(this::toDetailLabelDto).toList())
                .subtasks(task.getSubtasks().stream().map(this::toSubtaskDto).toList())
                .attachments(task.getAttachments().stream().map(this::toAttachmentDto).toList())
                .comments(task.getComments().stream().map(this::toCommentDto).toList())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private TaskDetailResponseDto.UserSummaryDto toUserSummaryDto(User user) {
        return TaskDetailResponseDto.UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    private TaskDetailResponseDto.LabelDto toDetailLabelDto(Label label) {
        return TaskDetailResponseDto.LabelDto.builder()
                .id(label.getId())
                .name(label.getName())
                .color(label.getColor())
                .build();
    }

    private TaskDetailResponseDto.SubtaskDto toSubtaskDto(Subtask subtask) {
        return TaskDetailResponseDto.SubtaskDto.builder()
                .id(subtask.getId())
                .title(subtask.getTitle())
                .done(subtask.isDone())
                .build();
    }

    private TaskDetailResponseDto.AttachmentDto toAttachmentDto(Attachment attachment) {
        return TaskDetailResponseDto.AttachmentDto.builder()
                .id(attachment.getId())
                .filename(attachment.getFilename())
                .contentType(attachment.getContentType())
                .extension(attachment.getExtension())
                .sizeBytes(attachment.getSizeBytes())
                .status(attachment.getStatus())
                .uploadedBy(toUserSummaryDto(attachment.getUploadedBy()))
                .createdAt(attachment.getCreatedAt())
                .build();
    }

    private TaskDetailResponseDto.CommentDto toCommentDto(Comment comment) {
        return TaskDetailResponseDto.CommentDto.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .author(toUserSummaryDto(comment.getAuthor()))
                .editedAt(comment.getEditedAt())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public SubtaskResponseDto toSubtaskResponseDto(Subtask subtask) {
        return SubtaskResponseDto.builder()
                .id(subtask.getId())
                .taskId(subtask.getTask().getId())
                .title(subtask.getTitle())
                .done(subtask.isDone())
                .createdAt(subtask.getCreatedAt())
                .updatedAt(subtask.getUpdatedAt())
                .build();
    }

    public AddCommentResponseDto toAddCommentResponseDto(Comment comment) {
        return AddCommentResponseDto.builder()
                .id(comment.getId())
                .taskId(comment.getTask().getId())
                .body(comment.getBody())
                .author(toAddCommentAuthorDto(comment.getAuthor()))
                .editedAt(comment.getEditedAt())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    private AddCommentResponseDto.AuthorDto toAddCommentAuthorDto(User user) {
        return AddCommentResponseDto.AuthorDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
