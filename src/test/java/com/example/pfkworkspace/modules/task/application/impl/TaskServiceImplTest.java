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
import com.example.pfkworkspace.modules.task.application.mapper.TaskLabelMapper;
import com.example.pfkworkspace.modules.task.application.mapper.TaskMapper;
import com.example.pfkworkspace.modules.task.domain.Task;
import com.example.pfkworkspace.modules.task.domain.TaskPriority;
import com.example.pfkworkspace.modules.task.domain.TaskStatus;
import com.example.pfkworkspace.modules.task.infrastructure.repo.TaskRepository;
import com.example.pfkworkspace.modules.user.api.exception.UserNotFoundException;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.user.infrastructure.repo.UserRepository;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceMember;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskAccessService taskAccessService;
    @Mock
    private TaskLabelMapper taskLabelMapper;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private UUID workspaceId;
    private UUID projectId;
    private UUID taskId;
    private Project project;
    private User assignedUser;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        Workspace workspace = Workspace.builder().name("Workspace").build();
        workspace.setId(workspaceId);

        project = Project.builder().name("Project").workspace(workspace).build();
        project.setId(projectId);

        assignedUser = User.builder()
                .email("assignee@example.com")
                .username("assignee")
                .firstName("Assignee")
                .lastName("User")
                .build();
        assignedUser.setId(UUID.randomUUID());
    }

    private CreateTaskRequestDto createTaskRequest() {
        return new CreateTaskRequestDto(
                "New Task", "Description", TaskStatus.TODO, TaskPriority.MEDIUM,
                assignedUser.getId(), LocalDate.now().plusDays(3), Set.of());
    }

    private Task taskInProject() {
        Task task = Task.builder()
                .project(project)
                .workspace(project.getWorkspace())
                .title("Existing Task")
                .description("Existing description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .assignee(assignedUser)
                .dueDate(LocalDate.now().plusDays(1))
                .build();
        task.setId(taskId);
        return task;
    }

    @Test
    void createTask_WhenAssignedUserNotFound_ShouldThrow() {
        when(taskAccessService.getProjectInWorkspace(workspaceId, projectId)).thenReturn(project);
        when(userRepository.findById(assignedUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(createTaskRequest(), workspaceId, projectId))
                .isInstanceOf(UserNotFoundException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_WhenAssignedUserNotWorkspaceMember_ShouldThrow() {
        when(taskAccessService.getProjectInWorkspace(workspaceId, projectId)).thenReturn(project);
        when(userRepository.findById(assignedUser.getId())).thenReturn(Optional.of(assignedUser));
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(assignedUser.getId(), workspaceId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(createTaskRequest(), workspaceId, projectId))
                .isInstanceOf(UnauthorizedException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_WhenValid_ShouldCreateSuccessfully() {
        CreateTaskRequestDto request = createTaskRequest();
        CreateTaskResponseDto response = CreateTaskResponseDto.builder().id(taskId).title("New Task").build();

        when(taskAccessService.getProjectInWorkspace(workspaceId, projectId)).thenReturn(project);
        when(userRepository.findById(assignedUser.getId())).thenReturn(Optional.of(assignedUser));
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(assignedUser.getId(), workspaceId))
                .thenReturn(Optional.of(mock(WorkspaceMember.class)));
        when(taskLabelMapper.toEntities(request.labelIds(), projectId)).thenReturn(Set.of());
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(taskId);
            return task;
        });
        when(taskMapper.toCreateResponseDto(any(Task.class))).thenReturn(response);

        CreateTaskResponseDto result = taskService.createTask(request, workspaceId, projectId);

        assertThat(result).isEqualTo(response);
        assertThat(project.getTasks()).hasSize(1);
        assertThat(project.getTasks().get(0).getTitle()).isEqualTo("New Task");
        assertThat(project.getTasks().get(0).getAssignee()).isEqualTo(assignedUser);
    }

    @Test
    void getTasks_ShouldValidateProjectAndReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new org.springframework.data.domain.PageImpl<>(java.util.List.of(taskInProject()));
        Page<TaskResponseDto> responsePage =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(TaskResponseDto.builder().id(taskId).build()));

        when(taskRepository.findAllByProjectId(projectId, pageable)).thenReturn(taskPage);
        when(taskMapper.toResponsePage(taskPage)).thenReturn(responsePage);

        Page<TaskResponseDto> result = taskService.getTasks(workspaceId, projectId, pageable);

        assertThat(result).isEqualTo(responsePage);
        verify(taskAccessService).getProjectInWorkspace(workspaceId, projectId);
    }

    @Test
    void getTaskDetail_ShouldReturnMappedDetail() {
        Task task = taskInProject();
        TaskDetailResponseDto detail = TaskDetailResponseDto.builder().id(taskId).build();

        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
        when(taskMapper.toDetailDto(task)).thenReturn(detail);

        TaskDetailResponseDto result = taskService.getTaskDetail(workspaceId, projectId, taskId);

        assertThat(result).isEqualTo(detail);
    }

    @Test
    void updateTaskStatus_ShouldUpdateStatusAndReturnDetail() {
        Task task = taskInProject();
        UpdateTaskStatusRequestDto request = new UpdateTaskStatusRequestDto(TaskStatus.DONE);
        TaskDetailResponseDto detail = TaskDetailResponseDto.builder().id(taskId).status(TaskStatus.DONE).build();

        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDetailDto(task)).thenReturn(detail);

        TaskDetailResponseDto result = taskService.updateTaskStatus(workspaceId, projectId, taskId, request);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(result).isEqualTo(detail);
    }

    @Test
    void updateTaskAssignee_WhenAssigneeIdIsNull_ShouldClearAssignee() {
        Task task = taskInProject();
        UpdateTaskAssigneeRequestDto request = new UpdateTaskAssigneeRequestDto(null);
        TaskDetailResponseDto detail = TaskDetailResponseDto.builder().id(taskId).build();

        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDetailDto(task)).thenReturn(detail);

        taskService.updateTaskAssignee(workspaceId, projectId, taskId, request);

        assertThat(task.getAssignee()).isNull();
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateTaskAssignee_WhenAssigneeNotWorkspaceMember_ShouldThrow() {
        Task task = taskInProject();
        UUID newAssigneeId = UUID.randomUUID();
        User newAssignee = User.builder().email("new@example.com").username("new").build();
        newAssignee.setId(newAssigneeId);
        UpdateTaskAssigneeRequestDto request = new UpdateTaskAssigneeRequestDto(newAssigneeId);

        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
        when(userRepository.findById(newAssigneeId)).thenReturn(Optional.of(newAssignee));
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(newAssigneeId, workspaceId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTaskAssignee(workspaceId, projectId, taskId, request))
                .isInstanceOf(UnauthorizedException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTaskAssignee_WhenValid_ShouldSetAssignee() {
        Task task = taskInProject();
        UUID newAssigneeId = UUID.randomUUID();
        User newAssignee = User.builder().email("new@example.com").username("new").build();
        newAssignee.setId(newAssigneeId);
        UpdateTaskAssigneeRequestDto request = new UpdateTaskAssigneeRequestDto(newAssigneeId);
        TaskDetailResponseDto detail = TaskDetailResponseDto.builder().id(taskId).build();

        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
        when(userRepository.findById(newAssigneeId)).thenReturn(Optional.of(newAssignee));
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(newAssigneeId, workspaceId))
                .thenReturn(Optional.of(mock(WorkspaceMember.class)));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDetailDto(task)).thenReturn(detail);

        taskService.updateTaskAssignee(workspaceId, projectId, taskId, request);

        assertThat(task.getAssignee()).isEqualTo(newAssignee);
    }

    @Test
    void updateTask_ShouldOnlyPatchNonNullFields() {
        Task task = taskInProject();
        LocalDate originalDueDate = task.getDueDate();
        UpdateTaskRequestDto request = new UpdateTaskRequestDto("Updated Title", null, null, null);
        TaskDetailResponseDto detail = TaskDetailResponseDto.builder().id(taskId).build();

        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDetailDto(task)).thenReturn(detail);

        taskService.updateTask(workspaceId, projectId, taskId, request);

        assertThat(task.getTitle()).isEqualTo("Updated Title");
        assertThat(task.getDescription()).isEqualTo("Existing description");
        assertThat(task.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(task.getDueDate()).isEqualTo(originalDueDate);
    }

    @Test
    void deleteTask_ShouldDeleteTask() {
        Task task = taskInProject();
        when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);

        taskService.deleteTask(workspaceId, projectId, taskId);

        verify(taskRepository).delete(task);
    }
}
