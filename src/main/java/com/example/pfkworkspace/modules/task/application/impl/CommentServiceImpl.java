package com.example.pfkworkspace.modules.task.application.impl;

import com.example.pfkworkspace.common.error.UnauthorizedException;
import com.example.pfkworkspace.modules.auth.application.UserContextService;
import com.example.pfkworkspace.modules.task.api.dto.request.AddCommentRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.AddCommentResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.RemoveCommentResponseDto;
import com.example.pfkworkspace.modules.task.api.exception.CommentNotFoundException;
import com.example.pfkworkspace.modules.task.application.CommentService;
import com.example.pfkworkspace.modules.task.application.TaskAccessService;
import com.example.pfkworkspace.modules.task.application.annotation.EvictTaskCache;
import com.example.pfkworkspace.modules.task.application.mapper.TaskMapper;
import com.example.pfkworkspace.modules.task.domain.Comment;
import com.example.pfkworkspace.modules.task.domain.Task;
import com.example.pfkworkspace.modules.task.infrastructure.repo.CommentRepository;
import com.example.pfkworkspace.modules.task.infrastructure.repo.TaskRepository;
import com.example.pfkworkspace.modules.user.domain.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final TaskAccessService taskAccessService;
    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final UserContextService userContextService;

    @Override
    @PreAuthorize("@workspaceSecurity.isMember(#workspaceId)")
    @Transactional
    @EvictTaskCache
    public AddCommentResponseDto addCommentToTask(
            @P("workspaceId") UUID workspaceId,
            UUID projectId,
            UUID taskId,
            AddCommentRequestDto requestDto) {
        Task task = taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId);
        User currentUser = userContextService.getCurrentUser();

        Comment newComment = Comment.builder().body(requestDto.body()).author(currentUser).build();
        task.addComment(newComment);
        taskRepository.save(task);

        log.info("Comment added to task {}: {}", taskId, newComment.getId());

        return taskMapper.toAddCommentResponseDto(newComment);
    }

    @Override
    @PreAuthorize("@workspaceSecurity.isMember(#workspaceId)")
    @Transactional
    @EvictTaskCache
    public RemoveCommentResponseDto removeCommentFromTask(
            @P("workspaceId") UUID workspaceId, UUID projectId, UUID taskId, UUID commentId) {
        Task task = taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId);

        Comment comment =
                commentRepository
                        .findById(commentId)
                        .orElseThrow(
                                () -> new CommentNotFoundException("Comment not found with id: " + commentId));

        User currentUser = userContextService.getCurrentUser();
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to remove this comment");
        }

        task.removeComment(comment);
        taskRepository.save(task);
        log.info("Comment removed from task {}: {}", taskId, commentId);

        return RemoveCommentResponseDto.builder().id(commentId).taskId(taskId).build();
    }
}
