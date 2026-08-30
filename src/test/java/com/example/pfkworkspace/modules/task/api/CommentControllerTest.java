package com.example.pfkworkspace.modules.task.api;

import com.example.pfkworkspace.common.error.ApiAccessDeniedHandler;
import com.example.pfkworkspace.common.error.ApiAuthenticationEntryPoint;
import com.example.pfkworkspace.modules.auth.application.impl.UserDetailsServiceImpl;
import com.example.pfkworkspace.modules.auth.infrastructure.CustomLogoutHandler;
import com.example.pfkworkspace.modules.auth.infrastructure.JwtAuthFilter;
import com.example.pfkworkspace.modules.task.api.dto.request.AddCommentRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.AddCommentResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.RemoveCommentResponseDto;
import com.example.pfkworkspace.modules.task.application.CommentService;
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

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

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
    void addComment_ShouldReturnOk() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        AddCommentRequestDto request = new AddCommentRequestDto("Hello world");
        AddCommentResponseDto response = AddCommentResponseDto.builder()
                .id(UUID.randomUUID())
                .taskId(taskId)
                .body("Hello world")
                .build();

        when(commentService.addCommentToTask(eq(workspaceId), eq(projectId), eq(taskId), any(AddCommentRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/comments",
                        workspaceId, projectId, taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.body").value("Hello world"))
                .andExpect(jsonPath("$.message").value("Comment added successfully"));
    }

    @Test
    @WithMockUser
    void removeComment_ShouldReturnOk() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        RemoveCommentResponseDto response = RemoveCommentResponseDto.builder().id(commentId).taskId(taskId).build();

        when(commentService.removeCommentFromTask(workspaceId, projectId, taskId, commentId)).thenReturn(response);

        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/comments/{commentId}",
                        workspaceId, projectId, taskId, commentId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(commentId.toString()))
                .andExpect(jsonPath("$.message").value("Comment removed successfully"));
    }
}
