package com.example.pfkworkspace.modules.task.application.impl;

import com.example.pfkworkspace.modules.task.api.dto.request.AddSubtaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateSubtaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.RemoveSubtaskResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.SubtaskResponseDto;
import com.example.pfkworkspace.modules.task.api.exception.SubtaskNotFoundException;
import com.example.pfkworkspace.modules.task.application.SubtaskService;
import com.example.pfkworkspace.modules.task.application.TaskAccessService;
import com.example.pfkworkspace.modules.task.application.annotation.EvictTaskCache;
import com.example.pfkworkspace.modules.task.application.mapper.TaskMapper;
import com.example.pfkworkspace.modules.task.domain.Subtask;
import com.example.pfkworkspace.modules.task.domain.Task;
import com.example.pfkworkspace.modules.task.infrastructure.repo.SubtaskRepository;
import com.example.pfkworkspace.modules.task.infrastructure.repo.TaskRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubtaskServiceImpl implements SubtaskService {

    private final TaskAccessService taskAccessService;
    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;

    @Override
    @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
    @Transactional
    @EvictTaskCache
    public SubtaskResponseDto addSubtask(
            @P("workspaceId") UUID workspaceId,
            UUID projectId,
            UUID taskId,
            AddSubtaskRequestDto requestDto) {
        Task task = taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId);

        Subtask newSubtask = Subtask.builder().title(requestDto.title()).done(false).build();
        task.addSubtask(newSubtask);
        taskRepository.save(task);

        return taskMapper.toSubtaskResponseDto(newSubtask);
    }

    @Override
    @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
    @Transactional
    @EvictTaskCache
    public RemoveSubtaskResponseDto removeSubtask(
            @P("workspaceId") UUID workspaceId, UUID projectId, UUID taskId, UUID subtaskId) {
        Task task = taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId);
        Subtask subtask = getSubtaskInTask(taskId, subtaskId);

        task.removeSubtask(subtask);
        taskRepository.save(task);

        return RemoveSubtaskResponseDto.builder().id(subtaskId).taskId(taskId).build();
    }

    @Override
    @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
    @Transactional
    @EvictTaskCache
    public SubtaskResponseDto updateSubtask(
            @P("workspaceId") UUID workspaceId,
            UUID projectId,
            UUID taskId,
            UUID subtaskId,
            UpdateSubtaskRequestDto requestDto) {
        taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId);
        Subtask subtask = getSubtaskInTask(taskId, subtaskId);

        if (requestDto.title() != null) {
            subtask.setTitle(requestDto.title());
        }
        if (requestDto.done() != null) {
            subtask.setDone(requestDto.done());
        }

        Subtask updatedSubtask = subtaskRepository.save(subtask);
        return taskMapper.toSubtaskResponseDto(updatedSubtask);
    }

    private Subtask getSubtaskInTask(UUID taskId, UUID subtaskId) {
        return subtaskRepository
                .findByIdAndTaskId(subtaskId, taskId)
                .orElseThrow(() -> new SubtaskNotFoundException("Subtask not found with id: " + subtaskId));
    }
}
