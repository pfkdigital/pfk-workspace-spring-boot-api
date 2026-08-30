package com.example.pfkworkspace.modules.task.application.impl;

import com.example.pfkworkspace.modules.task.api.dto.request.AddSubtaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateSubtaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.RemoveSubtaskResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.SubtaskResponseDto;
import com.example.pfkworkspace.modules.task.api.exception.SubtaskNotFoundException;
import com.example.pfkworkspace.modules.task.application.TaskAccessService;
import com.example.pfkworkspace.modules.task.application.mapper.TaskMapper;
import com.example.pfkworkspace.modules.task.domain.Subtask;
import com.example.pfkworkspace.modules.task.domain.Task;
import com.example.pfkworkspace.modules.task.domain.TaskPriority;
import com.example.pfkworkspace.modules.task.domain.TaskStatus;
import com.example.pfkworkspace.modules.task.infrastructure.repo.SubtaskRepository;
import com.example.pfkworkspace.modules.task.infrastructure.repo.TaskRepository;
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
class SubtaskServiceImplTest {

    @Mock
    private TaskAccessService taskAccessService;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private SubtaskRepository subtaskRepository;

    @InjectMocks
    private SubtaskServiceImpl subtaskService;

    private UUID workspaceId;
    private UUID projectId;
    private UUID taskId;
    private Task task;

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
    }

    @Test
    void addSubtask_ShouldAddSubtaskToTaskAndReturnDto() {
        AddSubtaskRequestDto request = new AddSubtaskRequestDto("New Subtask");
        SubtaskResponseDto response = SubtaskResponseDto.builder().taskId(taskId).title("New Subtask").build();

        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
        when(taskMapper.toSubtaskResponseDto(any(Subtask.class))).thenReturn(response);

        SubtaskResponseDto result = subtaskService.addSubtask(workspaceId, projectId, taskId, request);

        assertThat(result).isEqualTo(response);
        assertThat(task.getSubtasks()).hasSize(1);
        assertThat(task.getSubtasks().get(0).getTitle()).isEqualTo("New Subtask");
        assertThat(task.getSubtasks().get(0).isDone()).isFalse();
        verify(taskRepository).save(task);
    }

    @Test
    void removeSubtask_WhenSubtaskNotFound_ShouldThrow() {
        UUID subtaskId = UUID.randomUUID();
        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
        when(subtaskRepository.findByIdAndTaskId(subtaskId, taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subtaskService.removeSubtask(workspaceId, projectId, taskId, subtaskId))
                .isInstanceOf(SubtaskNotFoundException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    void removeSubtask_WhenFound_ShouldRemoveFromTaskAndReturnDto() {
        UUID subtaskId = UUID.randomUUID();
        Subtask subtask = Subtask.builder().title("Existing").done(false).build();
        subtask.setId(subtaskId);
        task.addSubtask(subtask);

        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
        when(subtaskRepository.findByIdAndTaskId(subtaskId, taskId)).thenReturn(Optional.of(subtask));

        RemoveSubtaskResponseDto result = subtaskService.removeSubtask(workspaceId, projectId, taskId, subtaskId);

        assertThat(result.getId()).isEqualTo(subtaskId);
        assertThat(result.getTaskId()).isEqualTo(taskId);
        assertThat(task.getSubtasks()).isEmpty();
        verify(taskRepository).save(task);
    }

    @Test
    void updateSubtask_WhenSubtaskNotFound_ShouldThrow() {
        UUID subtaskId = UUID.randomUUID();
        UpdateSubtaskRequestDto request = new UpdateSubtaskRequestDto("Updated", true);
        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
        when(subtaskRepository.findByIdAndTaskId(subtaskId, taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subtaskService.updateSubtask(workspaceId, projectId, taskId, subtaskId, request))
                .isInstanceOf(SubtaskNotFoundException.class);
    }

    @Test
    void updateSubtask_WhenValid_ShouldPatchFieldsAndReturnDto() {
        UUID subtaskId = UUID.randomUUID();
        Subtask subtask = Subtask.builder().title("Old title").done(false).build();
        subtask.setId(subtaskId);
        UpdateSubtaskRequestDto request = new UpdateSubtaskRequestDto("Updated title", true);
        SubtaskResponseDto response = SubtaskResponseDto.builder().id(subtaskId).title("Updated title").done(true).build();

        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
        when(subtaskRepository.findByIdAndTaskId(subtaskId, taskId)).thenReturn(Optional.of(subtask));
        when(subtaskRepository.save(subtask)).thenReturn(subtask);
        when(taskMapper.toSubtaskResponseDto(subtask)).thenReturn(response);

        SubtaskResponseDto result = subtaskService.updateSubtask(workspaceId, projectId, taskId, subtaskId, request);

        assertThat(result).isEqualTo(response);
        assertThat(subtask.getTitle()).isEqualTo("Updated title");
        assertThat(subtask.isDone()).isTrue();
    }

    @Test
    void updateSubtask_WhenFieldsAreNull_ShouldNotChangeExistingValues() {
        UUID subtaskId = UUID.randomUUID();
        Subtask subtask = Subtask.builder().title("Old title").done(false).build();
        subtask.setId(subtaskId);
        UpdateSubtaskRequestDto request = new UpdateSubtaskRequestDto(null, null);
        SubtaskResponseDto response = SubtaskResponseDto.builder().id(subtaskId).build();

        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
        when(subtaskRepository.findByIdAndTaskId(subtaskId, taskId)).thenReturn(Optional.of(subtask));
        when(subtaskRepository.save(subtask)).thenReturn(subtask);
        when(taskMapper.toSubtaskResponseDto(subtask)).thenReturn(response);

        subtaskService.updateSubtask(workspaceId, projectId, taskId, subtaskId, request);

        assertThat(subtask.getTitle()).isEqualTo("Old title");
        assertThat(subtask.isDone()).isFalse();
    }
}
