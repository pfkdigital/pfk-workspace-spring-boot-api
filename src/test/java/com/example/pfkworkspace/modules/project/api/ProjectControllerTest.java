package com.example.pfkworkspace.modules.project.api;

import com.example.pfkworkspace.common.error.ApiAccessDeniedHandler;
import com.example.pfkworkspace.common.error.ApiAuthenticationEntryPoint;
import com.example.pfkworkspace.modules.auth.application.impl.UserDetailsServiceImpl;
import com.example.pfkworkspace.modules.auth.infrastructure.CustomLogoutHandler;
import com.example.pfkworkspace.modules.auth.infrastructure.JwtAuthFilter;
import com.example.pfkworkspace.modules.project.api.dto.*;
import com.example.pfkworkspace.modules.project.application.ProjectService;
import com.example.pfkworkspace.modules.project.domain.ProjectStatus;
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
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

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
    void createProject_ShouldReturnCreated() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        CreateProjectDtoRequest request = new CreateProjectDtoRequest(
                "Test Project", "Desc", "#10B981", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1));
        CreateProjectResponseDto response = CreateProjectResponseDto.builder()
                .id(UUID.randomUUID())
                .name("Test Project")
                .build();

        when(projectService.createProject(eq(workspaceId), any(CreateProjectDtoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/projects", workspaceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPanel("success").value(true))
                .andExpect(jsonPanel("data.name").value("Test Project"));
    }

    @Test
    @WithMockUser
    void getProjects_ShouldReturnList() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        ProjectResponseDto summary = ProjectResponseDto.builder()
                .id(UUID.randomUUID())
                .name("Test Project")
                .build();

        when(projectService.getProjects(workspaceId)).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/projects", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("success").value(true))
                .andExpect(jsonPanel("data[0].name").value("Test Project"));
    }

    @Test
    @WithMockUser
    void getProjectDetail_ShouldReturnDetail() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        ProjectDetailResponseDto detail = ProjectDetailResponseDto.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectService.getProjectDetail(workspaceId, projectId)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/projects/{projectId}", workspaceId, projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("data.name").value("Test Project"));
    }

    @Test
    @WithMockUser
    void updateProject_ShouldReturnUpdated() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UpdateProjectRequestDto request = new UpdateProjectRequestDto(
                "Updated Name", "Updated Desc", null, ProjectStatus.ACTIVE, null, null);
        UpdateProjectResponseDto response = UpdateProjectResponseDto.builder()
                .id(projectId)
                .name("Updated Name")
                .build();

        when(projectService.updateProject(eq(workspaceId), eq(projectId), any(UpdateProjectRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/projects/{projectId}", workspaceId, projectId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("data.name").value("Updated Name"));
    }

    @Test
    @WithMockUser
    void deleteProject_ShouldReturnOk() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/projects/{projectId}", workspaceId, projectId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("message").value("Project deleted successfully"));
    }

    @Test
    @WithMockUser
    void archiveProject_ShouldReturnOk() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        ArchiveProjectResponseDto response = ArchiveProjectResponseDto.builder()
                .id(projectId)
                .status(ProjectStatus.ARCHIVED)
                .build();

        when(projectService.archiveProject(workspaceId, projectId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/projects/{projectId}/archive", workspaceId, projectId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("message").value("Project archived successfully"))
                .andExpect(jsonPanel("data.status").value("ARCHIVED"));
    }

    private JsonPathResultMatchers jsonPanel(String path) {
        return jsonPath("$." + path);
    }
}
