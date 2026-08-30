package com.example.pfkworkspace.modules.task.application;

import com.example.pfkworkspace.common.error.UnauthorizedException;
import com.example.pfkworkspace.modules.project.api.exception.ProjectNotFoundException;
import com.example.pfkworkspace.modules.project.domain.Project;
import com.example.pfkworkspace.modules.project.infrastructure.repo.ProjectRepository;
import com.example.pfkworkspace.modules.task.api.exception.TaskNotFoundException;
import com.example.pfkworkspace.modules.task.domain.Task;
import com.example.pfkworkspace.modules.task.infrastructure.repo.TaskRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskAccessService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public Task getTaskInWorkspaceProject(UUID workspaceId, UUID projectId, UUID taskId) {
        getProjectInWorkspace(workspaceId, projectId);
        return getTaskInProject(projectId, taskId);
    }

    public Project getProjectInWorkspace(UUID workspaceId, UUID projectId) {
        Project project =
                projectRepository
                        .findById(projectId)
                        .orElseThrow(
                                () -> new ProjectNotFoundException("Project not found with id: " + projectId));

        if (!project.getWorkspace().getId().equals(workspaceId)) {
            throw new UnauthorizedException("You are not authorized to access this project");
        }

        return project;
    }

    public Task getTaskInProject(UUID projectId, UUID taskId) {
        return taskRepository
                .findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
    }
}
