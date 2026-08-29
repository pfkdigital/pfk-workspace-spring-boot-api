package com.example.pfkworkspace.modules.task.application.impl;

import com.example.pfkworkspace.common.error.UnauthorizedException;
import com.example.pfkworkspace.modules.project.api.exception.ProjectNotFoundException;
import com.example.pfkworkspace.modules.project.domain.Project;
import com.example.pfkworkspace.modules.project.infrastructure.repo.ProjectRepository;
import com.example.pfkworkspace.modules.task.api.dto.request.AddSubtaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.CreateTaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateSubtaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateTaskAssigneeRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateTaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateTaskStatusRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.CreateTaskResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.RemoveSubtaskResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.SubtaskResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.TaskDetailResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.TaskResponseDto;
import com.example.pfkworkspace.modules.task.api.exception.SubtaskNotFoundException;
import com.example.pfkworkspace.modules.task.api.exception.TaskNotFoundException;
import com.example.pfkworkspace.modules.task.application.TaskService;
import com.example.pfkworkspace.modules.task.application.mapper.TaskLabelMapper;
import com.example.pfkworkspace.modules.task.application.mapper.TaskMapper;
import com.example.pfkworkspace.modules.task.domain.Subtask;
import com.example.pfkworkspace.modules.task.domain.Task;
import com.example.pfkworkspace.modules.task.infrastructure.repo.SubtaskRepository;
import com.example.pfkworkspace.modules.task.infrastructure.repo.TaskRepository;
import com.example.pfkworkspace.modules.user.api.exception.UserNotFoundException;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.user.infrastructure.repo.UserRepository;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Cache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

  private final TaskLabelMapper taskLabelMapper;
  private final TaskMapper taskMapper;
  private final ProjectRepository projectRepository;
  private final TaskRepository taskRepository;
  private final SubtaskRepository subtaskRepository;
  private final UserRepository userRepository;
  private final WorkspaceMemberRepository workspaceMemberRepository;

  @Override
  @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
  @Transactional
  @CacheEvict(value = "tasks", key = "#workspaceId + '_' + #projectId")
  public CreateTaskResponseDto createTask(
      CreateTaskRequestDto requestDto, @P("workspaceId") UUID workspaceId, UUID projectId) {
    Project project = getProjectInWorkspace(workspaceId, projectId);

    User assignedUser =
        userRepository
            .findById(requestDto.assignedTo())
            .orElseThrow(
                () ->
                    new UserNotFoundException(
                        "User not found with id: " + requestDto.assignedTo()));

    workspaceMemberRepository
        .findByUserIdAndWorkspaceId(assignedUser.getId(), workspaceId)
        .orElseThrow(
            () -> new UnauthorizedException("Assigned user is not a member of this workspace"));

    Task newTask =
        Task.builder()
            .workspace(project.getWorkspace())
            .title(requestDto.title())
            .description(requestDto.description())
            .status(requestDto.taskStatus())
            .priority(requestDto.taskPriority())
            .assignee(assignedUser)
            .dueDate(requestDto.dueDate())
            .labels(taskLabelMapper.toEntities(requestDto.labelIds(), projectId))
            .build();

    project.addTask(newTask);
    Task savedTask = taskRepository.save(newTask);

    return taskMapper.toCreateResponseDto(savedTask);
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isMember(#workspaceId)")
  @Cacheable(value = "tasks", key = "#workspaceId + '_' + #projectId")
  public Page<TaskResponseDto> getTasks(
      @P("workspaceId") UUID workspaceId, UUID projectId, Pageable pageable) {
    getProjectInWorkspace(workspaceId, projectId);

    Page<Task> tasks = taskRepository.findAllByProjectId(projectId, pageable);
    return taskMapper.toResponsePage(tasks);
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isMember(#workspaceId)")
  @Transactional(readOnly = true)
  @Cacheable(value = "tasks", key = "#workspaceId + '_' + #projectId + '_' + #taskId")
  public TaskDetailResponseDto getTaskDetail(
      @P("workspaceId") UUID workspaceId, UUID projectId, UUID taskId) {
    getProjectInWorkspace(workspaceId, projectId);
    Task task = getTaskInProject(projectId, taskId);

    return taskMapper.toDetailDto(task);
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "tasks", key = "#workspaceId + '_' + #projectId + '_' + #taskId"),
        @CacheEvict(value = "tasks", key = "#workspaceId + '_' + #projectId")
      })
  public TaskDetailResponseDto updateTaskStatus(
      @P("workspaceId") UUID workspaceId,
      UUID projectId,
      UUID taskId,
      UpdateTaskStatusRequestDto requestDto) {
    getProjectInWorkspace(workspaceId, projectId);
    Task task = getTaskInProject(projectId, taskId);

    task.setStatus(requestDto.status());

    Task updatedTask = taskRepository.save(task);
    return taskMapper.toDetailDto(updatedTask);
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "tasks", key = "#workspaceId + '_' + #projectId + '_' + #taskId"),
        @CacheEvict(value = "tasks", key = "#workspaceId + '_' + #projectId")
      })
  public TaskDetailResponseDto updateTaskAssignee(
      @P("workspaceId") UUID workspaceId,
      UUID projectId,
      UUID taskId,
      UpdateTaskAssigneeRequestDto requestDto) {
    getProjectInWorkspace(workspaceId, projectId);
    Task task = getTaskInProject(projectId, taskId);

    if (requestDto.assigneeId() == null) {
      task.setAssignee(null);
    } else {
      User assignee =
          userRepository
              .findById(requestDto.assigneeId())
              .orElseThrow(
                  () ->
                      new UserNotFoundException(
                          "User not found with id: " + requestDto.assigneeId()));

      workspaceMemberRepository
          .findByUserIdAndWorkspaceId(assignee.getId(), workspaceId)
          .orElseThrow(
              () -> new UnauthorizedException("Assigned user is not a member of this workspace"));

      task.setAssignee(assignee);
    }

    Task updatedTask = taskRepository.save(task);
    return taskMapper.toDetailDto(updatedTask);
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "tasks", key = "#workspaceId + '_' + #projectId + '_' + #taskId"),
        @CacheEvict(value = "tasks", key = "#workspaceId + '_' + #projectId")
      })
  public TaskDetailResponseDto updateTask(
      @P("workspaceId") UUID workspaceId,
      UUID projectId,
      UUID taskId,
      UpdateTaskRequestDto requestDto) {
    getProjectInWorkspace(workspaceId, projectId);
    Task task = getTaskInProject(projectId, taskId);

    if (requestDto.title() != null) {
      task.setTitle(requestDto.title());
    }
    if (requestDto.description() != null) {
      task.setDescription(requestDto.description());
    }
    if (requestDto.priority() != null) {
      task.setPriority(requestDto.priority());
    }
    if (requestDto.dueDate() != null) {
      task.setDueDate(requestDto.dueDate());
    }

    Task updatedTask = taskRepository.save(task);
    return taskMapper.toDetailDto(updatedTask);
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "tasks", key = "#workspaceId + '_' + #projectId + '_' + #taskId"),
        @CacheEvict(value = "tasks", key = "#workspaceId + '_' + #projectId")
      })
  public void deleteTask(@P("workspaceId") UUID workspaceId, UUID projectId, UUID taskId) {
    getProjectInWorkspace(workspaceId, projectId);
    Task task = getTaskInProject(projectId, taskId);

    taskRepository.delete(task);
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
  @Transactional
  @CacheEvict(value = "tasks", key = "#workspaceId + '_' + #projectId + '_' + #taskId")
  public SubtaskResponseDto addSubtask(
      @P("workspaceId") UUID workspaceId,
      UUID projectId,
      UUID taskId,
      AddSubtaskRequestDto requestDto) {
    getProjectInWorkspace(workspaceId, projectId);
    Task task = getTaskInProject(projectId, taskId);

    Subtask newSubtask = Subtask.builder().title(requestDto.title()).done(false).build();
    task.addSubtask(newSubtask);
    taskRepository.save(task);

    return taskMapper.toSubtaskResponseDto(newSubtask);
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
  @Transactional
  @CacheEvict(value = "tasks", key = "#workspaceId + '_' + #projectId + '_' + #taskId")
  public RemoveSubtaskResponseDto removeSubtask(
      @P("workspaceId") UUID workspaceId, UUID projectId, UUID taskId, UUID subtaskId) {
    getProjectInWorkspace(workspaceId, projectId);
    Task task = getTaskInProject(projectId, taskId);
    Subtask subtask = getSubtaskInTask(taskId, subtaskId);

    task.removeSubtask(subtask);
    taskRepository.save(task);

    return RemoveSubtaskResponseDto.builder().id(subtaskId).taskId(taskId).build();
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
  @Transactional
  @CacheEvict(value = "tasks", key = "#workspaceId + '_' + #projectId + '_' + #taskId")
  public SubtaskResponseDto updateSubtask(
      @P("workspaceId") UUID workspaceId,
      UUID projectId,
      UUID taskId,
      UUID subtaskId,
      UpdateSubtaskRequestDto requestDto) {
    getProjectInWorkspace(workspaceId, projectId);
    getTaskInProject(projectId, taskId);
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
        .orElseThrow(
            () -> new SubtaskNotFoundException("Subtask not found with id: " + subtaskId));
  }

  private Task getTaskInProject(UUID projectId, UUID taskId) {
    return taskRepository
        .findByIdAndProjectId(taskId, projectId)
        .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
  }

  private Project getProjectInWorkspace(UUID workspaceId, UUID projectId) {
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
}
