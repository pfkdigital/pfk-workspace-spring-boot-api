package com.example.pfkworkspace.modules.task.application.impl;

import com.example.pfkworkspace.common.error.UnauthorizedException;
import com.example.pfkworkspace.modules.project.domain.Project;
import com.example.pfkworkspace.modules.task.api.dto.request.CreateTaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateTaskAssigneeRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateTaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateTaskStatusRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.CreateTaskResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.TaskDetailResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.TaskResponseDto;
import com.example.pfkworkspace.modules.task.application.TaskAccessService;
import com.example.pfkworkspace.modules.task.application.TaskService;
import com.example.pfkworkspace.modules.task.application.annotation.EvictTaskAndListCache;
import com.example.pfkworkspace.modules.task.application.mapper.TaskLabelMapper;
import com.example.pfkworkspace.modules.task.application.mapper.TaskMapper;
import com.example.pfkworkspace.modules.task.domain.Task;
import com.example.pfkworkspace.modules.task.infrastructure.repo.TaskRepository;
import com.example.pfkworkspace.modules.user.api.exception.UserNotFoundException;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.user.infrastructure.repo.UserRepository;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

  private final TaskAccessService taskAccessService;
  private final TaskLabelMapper taskLabelMapper;
  private final TaskMapper taskMapper;
  private final TaskRepository taskRepository;
  private final UserRepository userRepository;
  private final WorkspaceMemberRepository workspaceMemberRepository;

  @Override
  @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
  @Transactional
  @CacheEvict(value = "tasks", key = "#workspaceId + '_' + #projectId")
  public CreateTaskResponseDto createTask(
      CreateTaskRequestDto requestDto, @P("workspaceId") UUID workspaceId, UUID projectId) {
    Project project = taskAccessService.getProjectInWorkspace(workspaceId, projectId);
    User assignedUser = getValidatedWorkspaceMember(requestDto.assignedTo(), workspaceId);

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
    taskAccessService.getProjectInWorkspace(workspaceId, projectId);

    Page<Task> tasks = taskRepository.findAllByProjectId(projectId, pageable);
    return taskMapper.toResponsePage(tasks);
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isMember(#workspaceId)")
  @Transactional(readOnly = true)
  @Cacheable(value = "tasks", key = "#workspaceId + '_' + #projectId + '_' + #taskId")
  public TaskDetailResponseDto getTaskDetail(
      @P("workspaceId") UUID workspaceId, UUID projectId, UUID taskId) {
    Task task = taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId);
    return taskMapper.toDetailDto(task);
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
  @Transactional
  @EvictTaskAndListCache
  public TaskDetailResponseDto updateTaskStatus(
      @P("workspaceId") UUID workspaceId,
      UUID projectId,
      UUID taskId,
      UpdateTaskStatusRequestDto requestDto) {
    Task task = taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId);

    task.setStatus(requestDto.status());

    Task updatedTask = taskRepository.save(task);
    return taskMapper.toDetailDto(updatedTask);
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
  @Transactional
  @EvictTaskAndListCache
  public TaskDetailResponseDto updateTaskAssignee(
      @P("workspaceId") UUID workspaceId,
      UUID projectId,
      UUID taskId,
      UpdateTaskAssigneeRequestDto requestDto) {
    Task task = taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId);

    if (requestDto.assigneeId() == null) {
      task.setAssignee(null);
    } else {
      task.setAssignee(getValidatedWorkspaceMember(requestDto.assigneeId(), workspaceId));
    }

    Task updatedTask = taskRepository.save(task);
    return taskMapper.toDetailDto(updatedTask);
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
  @Transactional
  @EvictTaskAndListCache
  public TaskDetailResponseDto updateTask(
      @P("workspaceId") UUID workspaceId,
      UUID projectId,
      UUID taskId,
      UpdateTaskRequestDto requestDto) {
    Task task = taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId);

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
  @EvictTaskAndListCache
  public void deleteTask(@P("workspaceId") UUID workspaceId, UUID projectId, UUID taskId) {
    Task task = taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId);
    taskRepository.delete(task);
  }

  private User getValidatedWorkspaceMember(UUID userId, UUID workspaceId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

    workspaceMemberRepository
        .findByUserIdAndWorkspaceId(user.getId(), workspaceId)
        .orElseThrow(
            () -> new UnauthorizedException("Assigned user is not a member of this workspace"));

    return user;
  }
}
