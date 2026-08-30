package com.example.pfkworkspace.modules.task.application;

import com.example.pfkworkspace.common.error.UnauthorizedException;
import com.example.pfkworkspace.modules.project.api.exception.ProjectNotFoundException;
import com.example.pfkworkspace.modules.project.domain.Project;
import com.example.pfkworkspace.modules.project.infrastructure.repo.ProjectRepository;
import com.example.pfkworkspace.modules.task.api.exception.TaskNotFoundException;
import com.example.pfkworkspace.modules.task.domain.Task;
import com.example.pfkworkspace.modules.task.infrastructure.repo.TaskRepository;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskAccessServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskAccessService taskAccessService;

    private UUID workspaceId;
    private UUID projectId;
    private UUID taskId;
    private Project project;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        Workspace workspace = Workspace.builder().name("Workspace").build();
        workspace.setId(workspaceId);

        project = Project.builder().name("Project").workspace(workspace).build();
        project.setId(projectId);
    }

    @Test
    void getProjectInWorkspace_WhenProjectNotFound_ShouldThrow() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskAccessService.getProjectInWorkspace(workspaceId, projectId))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void getProjectInWorkspace_WhenProjectBelongsToDifferentWorkspace_ShouldThrow() {
        Workspace otherWorkspace = Workspace.builder().name("Other").build();
        otherWorkspace.setId(UUID.randomUUID());
        project.setWorkspace(otherWorkspace);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> taskAccessService.getProjectInWorkspace(workspaceId, projectId))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getProjectInWorkspace_WhenValid_ShouldReturnProject() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        Project result = taskAccessService.getProjectInWorkspace(workspaceId, projectId);

        assertThat(result).isEqualTo(project);
    }

    @Test
    void getTaskInProject_WhenTaskNotFound_ShouldThrow() {
        when(taskRepository.findByIdAndProjectId(taskId, projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskAccessService.getTaskInProject(projectId, taskId))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void getTaskInProject_WhenFound_ShouldReturnTask() {
        Task task = Task.builder().project(project).build();
        task.setId(taskId);
        when(taskRepository.findByIdAndProjectId(taskId, projectId)).thenReturn(Optional.of(task));

        Task result = taskAccessService.getTaskInProject(projectId, taskId);

        assertThat(result).isEqualTo(task);
    }

    @Test
    void getTaskInWorkspaceProject_WhenValid_ShouldValidateProjectAndReturnTask() {
        Task task = Task.builder().project(project).build();
        task.setId(taskId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(taskRepository.findByIdAndProjectId(taskId, projectId)).thenReturn(Optional.of(task));

        Task result = taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId);

        assertThat(result).isEqualTo(task);
    }

    @Test
    void getTaskInWorkspaceProject_WhenProjectNotInWorkspace_ShouldThrowAndNotLookUpTask() {
        Workspace otherWorkspace = Workspace.builder().name("Other").build();
        otherWorkspace.setId(UUID.randomUUID());
        project.setWorkspace(otherWorkspace);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId))
                .isInstanceOf(UnauthorizedException.class);

        verifyNoInteractions(taskRepository);
    }
}
