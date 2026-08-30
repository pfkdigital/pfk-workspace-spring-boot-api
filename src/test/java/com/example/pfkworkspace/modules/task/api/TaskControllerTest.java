package com.example.pfkworkspace.modules.task.api;

import com.example.pfkworkspace.common.error.ApiAccessDeniedHandler;
import com.example.pfkworkspace.common.error.ApiAuthenticationEntryPoint;
import com.example.pfkworkspace.modules.auth.application.impl.UserDetailsServiceImpl;
import com.example.pfkworkspace.modules.auth.infrastructure.CustomLogoutHandler;
import com.example.pfkworkspace.modules.auth.infrastructure.JwtAuthFilter;
import com.example.pfkworkspace.modules.task.api.dto.request.CreateTaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateTaskAssigneeRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateTaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateTaskStatusRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.CreateTaskResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.TaskDetailResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.TaskResponseDto;
import com.example.pfkworkspace.modules.task.application.TaskService;
import com.example.pfkworkspace.modules.task.domain.TaskPriority;
import com.example.pfkworkspace.modules.task.domain.TaskStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;
    @MockitoBean
    private CustomLogoutHandler customLogoutHandler;
    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;
    @MockitoBean
    private ApiAccessDeniedHandler apiAccessDeniedHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createTask_ShouldReturnCreated() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        CreateTaskRequestDto request = new CreateTaskRequestDto(
                "Test Task", "Description", TaskStatus.TODO, TaskPriority.MEDIUM,
                UUID.randomUUID(), LocalDate.now().plusDays(3), Set.of());
        CreateTaskResponseDto response = CreateTaskResponseDto.builder()
                .id(UUID.randomUUID())
                .title("Test Task")
                .build();

        when(taskService.createTask(any(CreateTaskRequestDto.class), eq(workspaceId), eq(projectId)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/projects/{projectId}/tasks", workspaceId, projectId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Task"));
    }

    @Test
    @WithMockUser
    void getTasks_ShouldReturnPagedList() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        TaskResponseDto task = TaskResponseDto.builder().id(UUID.randomUUID()).title("Test Task").build();
        Page<TaskResponseDto> page = new PageImpl<>(List.of(task), PageRequest.of(0, 10), 1);

        when(taskService.getTasks(eq(workspaceId), eq(projectId), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/projects/{projectId}/tasks", workspaceId, projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Test Task"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser
    void getTaskDetail_ShouldReturnDetail() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        TaskDetailResponseDto detail = TaskDetailResponseDto.builder().id(taskId).title("Test Task").build();

        when(taskService.getTaskDetail(workspaceId, projectId, taskId)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}",
                        workspaceId, projectId, taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Test Task"));
    }

    @Test
    @WithMockUser
    void updateTaskStatus_ShouldReturnUpdatedDetail() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UpdateTaskStatusRequestDto request = new UpdateTaskStatusRequestDto(TaskStatus.DONE);
        TaskDetailResponseDto detail = TaskDetailResponseDto.builder().id(taskId).status(TaskStatus.DONE).build();

        when(taskService.updateTaskStatus(eq(workspaceId), eq(projectId), eq(taskId), any(UpdateTaskStatusRequestDto.class)))
                .thenReturn(detail);

        mockMvc.perform(patch("/api/v1/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/status",
                        workspaceId, projectId, taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DONE"));
    }

    @Test
    @WithMockUser
    void updateTaskAssignee_ShouldReturnUpdatedDetail() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UpdateTaskAssigneeRequestDto request = new UpdateTaskAssigneeRequestDto(UUID.randomUUID());
        TaskDetailResponseDto detail = TaskDetailResponseDto.builder().id(taskId).build();

        when(taskService.updateTaskAssignee(eq(workspaceId), eq(projectId), eq(taskId), any(UpdateTaskAssigneeRequestDto.class)))
                .thenReturn(detail);

        mockMvc.perform(patch("/api/v1/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/assignee",
                        workspaceId, projectId, taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(taskId.toString()));
    }

    @Test
    @WithMockUser
    void updateTask_ShouldReturnUpdatedDetail() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UpdateTaskRequestDto request = new UpdateTaskRequestDto("Updated Title", null, null, null);
        TaskDetailResponseDto detail = TaskDetailResponseDto.builder().id(taskId).title("Updated Title").build();

        when(taskService.updateTask(eq(workspaceId), eq(projectId), eq(taskId), any(UpdateTaskRequestDto.class)))
                .thenReturn(detail);

        mockMvc.perform(patch("/api/v1/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}",
                        workspaceId, projectId, taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    @WithMockUser
    void deleteTask_ShouldReturnOk() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}",
                        workspaceId, projectId, taskId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Task deleted successfully"));
    }
}
