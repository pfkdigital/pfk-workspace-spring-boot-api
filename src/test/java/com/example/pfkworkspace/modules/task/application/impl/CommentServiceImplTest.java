package com.example.pfkworkspace.modules.task.application.impl;

import com.example.pfkworkspace.common.error.UnauthorizedException;
import com.example.pfkworkspace.modules.auth.application.UserContextService;
import com.example.pfkworkspace.modules.task.api.dto.request.AddCommentRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.AddCommentResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.RemoveCommentResponseDto;
import com.example.pfkworkspace.modules.task.api.exception.CommentNotFoundException;
import com.example.pfkworkspace.modules.task.application.TaskAccessService;
import com.example.pfkworkspace.modules.task.application.mapper.TaskMapper;
import com.example.pfkworkspace.modules.task.domain.Comment;
import com.example.pfkworkspace.modules.task.domain.Task;
import com.example.pfkworkspace.modules.task.domain.TaskPriority;
import com.example.pfkworkspace.modules.task.domain.TaskStatus;
import com.example.pfkworkspace.modules.task.infrastructure.repo.CommentRepository;
import com.example.pfkworkspace.modules.task.infrastructure.repo.TaskRepository;
import com.example.pfkworkspace.modules.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private TaskAccessService taskAccessService;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserContextService userContextService;

    @InjectMocks
    private CommentServiceImpl commentService;

    private UUID workspaceId;
    private UUID projectId;
    private UUID taskId;
    private Task task;
    private User currentUser;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        task = Task.builder()
                .title("Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();
        task.setId(taskId);

        currentUser = User.builder().email("user@example.com").username("user").build();
        currentUser.setId(UUID.randomUUID());
    }

    @Test
    void addCommentToTask_ShouldAddCommentToTaskAndReturnDto() {
        AddCommentRequestDto request = new AddCommentRequestDto("Hello world");
        AddCommentResponseDto response = AddCommentResponseDto.builder().taskId(taskId).body("Hello world").build();

        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(taskMapper.toAddCommentResponseDto(any(Comment.class))).thenReturn(response);

        AddCommentResponseDto result = commentService.addCommentToTask(workspaceId, projectId, taskId, request);

        assertThat(result).isEqualTo(response);
        assertThat(task.getComments()).hasSize(1);
        assertThat(task.getComments().get(0).getBody()).isEqualTo("Hello world");
        assertThat(task.getComments().get(0).getAuthor()).isEqualTo(currentUser);
        verify(taskRepository).save(task);
    }

    @Test
    void removeCommentFromTask_WhenCommentNotFound_ShouldThrow() {
        UUID commentId = UUID.randomUUID();
        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.removeCommentFromTask(workspaceId, projectId, taskId, commentId))
                .isInstanceOf(CommentNotFoundException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    void removeCommentFromTask_WhenCurrentUserIsNotAuthor_ShouldThrowUnauthorized() {
        UUID commentId = UUID.randomUUID();
        User otherAuthor = User.builder().email("other@example.com").username("other").build();
        otherAuthor.setId(UUID.randomUUID());
        Comment comment = Comment.builder().body("Hello").author(otherAuthor).build();
        comment.setId(commentId);
        task.addComment(comment);

        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userContextService.getCurrentUser()).thenReturn(currentUser);

        assertThatThrownBy(() -> commentService.removeCommentFromTask(workspaceId, projectId, taskId, commentId))
                .isInstanceOf(UnauthorizedException.class);

        verify(taskRepository, never()).save(any());
        assertThat(task.getComments()).contains(comment);
    }

    @Test
    void removeCommentFromTask_WhenCurrentUserIsAuthor_ShouldRemoveCommentAndReturnDto() {
        UUID commentId = UUID.randomUUID();
        Comment comment = Comment.builder().body("Hello").author(currentUser).build();
        comment.setId(commentId);
        task.addComment(comment);

        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userContextService.getCurrentUser()).thenReturn(currentUser);

        RemoveCommentResponseDto result = commentService.removeCommentFromTask(workspaceId, projectId, taskId, commentId);

        assertThat(result.getId()).isEqualTo(commentId);
        assertThat(result.getTaskId()).isEqualTo(taskId);
        assertThat(task.getComments()).isEmpty();
        verify(taskRepository).save(task);
    }
}
