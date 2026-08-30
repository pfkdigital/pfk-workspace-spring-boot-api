package com.example.pfkworkspace.modules.task.api;

import com.example.pfkworkspace.common.error.ApiAccessDeniedHandler;
import com.example.pfkworkspace.common.error.ApiAuthenticationEntryPoint;
import com.example.pfkworkspace.modules.auth.application.impl.UserDetailsServiceImpl;
import com.example.pfkworkspace.modules.auth.infrastructure.CustomLogoutHandler;
import com.example.pfkworkspace.modules.auth.infrastructure.JwtAuthFilter;
import com.example.pfkworkspace.modules.task.api.dto.request.AddSubtaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateSubtaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.RemoveSubtaskResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.SubtaskResponseDto;
import com.example.pfkworkspace.modules.task.application.SubtaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubtaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class SubtaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubtaskService subtaskService;

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
    void addSubtasks_ShouldReturnOk() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        AddSubtaskRequestDto request = new AddSubtaskRequestDto("New Subtask");
        SubtaskResponseDto response = SubtaskResponseDto.builder()
                .id(UUID.randomUUID())
                .taskId(taskId)
                .title("New Subtask")
                .build();

        when(subtaskService.addSubtask(eq(workspaceId), eq(projectId), eq(taskId), any(AddSubtaskRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/subtasks",
                        workspaceId, projectId, taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("New Subtask"))
                .andExpect(jsonPath("$.message").value("Subtask added successfully"));
    }

    @Test
    @WithMockUser
    void removeSubtasks_ShouldReturnOk() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID subtaskId = UUID.randomUUID();
        RemoveSubtaskResponseDto response = RemoveSubtaskResponseDto.builder().id(subtaskId).taskId(taskId).build();

        when(subtaskService.removeSubtask(workspaceId, projectId, taskId, subtaskId)).thenReturn(response);

        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/subtasks/{subTaskId}",
                        workspaceId, projectId, taskId, subtaskId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(subtaskId.toString()))
                .andExpect(jsonPath("$.message").value("Subtask removed successfully"));
    }

    @Test
    @WithMockUser
    void updateSubTask_ShouldReturnOk() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID subtaskId = UUID.randomUUID();
        UpdateSubtaskRequestDto request = new UpdateSubtaskRequestDto("Updated title", true);
        SubtaskResponseDto response = SubtaskResponseDto.builder()
                .id(subtaskId)
                .taskId(taskId)
                .title("Updated title")
                .done(true)
                .build();

        when(subtaskService.updateSubtask(eq(workspaceId), eq(projectId), eq(taskId), eq(subtaskId), any(UpdateSubtaskRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/subtasks/{subTaskId}",
                        workspaceId, projectId, taskId, subtaskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated title"))
                .andExpect(jsonPath("$.data.done").value(true));
    }
}
