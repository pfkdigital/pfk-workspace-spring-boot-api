package com.example.pfkworkspace.modules.workspace.api;

import com.example.pfkworkspace.common.error.ApiAccessDeniedHandler;
import com.example.pfkworkspace.common.error.ApiAuthenticationEntryPoint;
import com.example.pfkworkspace.modules.auth.application.impl.UserDetailsServiceImpl;
import com.example.pfkworkspace.modules.auth.infrastructure.CustomLogoutHandler;
import com.example.pfkworkspace.modules.auth.infrastructure.JwtAuthFilter;
import com.example.pfkworkspace.modules.workspace.api.dto.*;
import com.example.pfkworkspace.modules.workspace.application.WorkspaceInvitationService;
import com.example.pfkworkspace.modules.workspace.application.WorkspaceService;
import com.example.pfkworkspace.modules.workspace.domain.UpdateMemberRole;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkspaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkspaceService workspaceService;

    @MockitoBean
    private WorkspaceInvitationService workspaceInvitationService;
    
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
    void createWorkspace_ShouldReturnCreated() throws Exception {
        CreateWorkspaceRequestDto request = new CreateWorkspaceRequestDto("Test Workspace", "Desc", "http://image.url");
        CreateWorkspaceResponseDto response = CreateWorkspaceResponseDto.builder()
                .id(UUID.randomUUID())
                .name("Test Workspace")
                .build();

        when(workspaceService.createWorkspace(any(CreateWorkspaceRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/workspace")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPanel("success").value(true))
                .andExpect(jsonPanel("data.name").value("Test Workspace"));
    }

    @Test
    @WithMockUser
    void getWorkspaces_ShouldReturnList() throws Exception {
        WorkspaceSummaryDto summary = WorkspaceSummaryDto.builder()
                .id(UUID.randomUUID())
                .name("Test Workspace")
                .build();
        when(workspaceService.getWorkspaces()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/workspace"))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("success").value(true))
                .andExpect(jsonPanel("data[0].name").value("Test Workspace"));
    }

    @Test
    @WithMockUser
    void getWorkspaceDetail_ShouldReturnDetail() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        WorkspaceDetailDto detail = WorkspaceDetailDto.builder()
                .id(workspaceId)
                .name("Test Workspace")
                .build();
        when(workspaceService.getWorkspaceDetail(workspaceId)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/workspace/{workspaceId}", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("data.name").value("Test Workspace"));
    }

    @Test
    @WithMockUser
    void updateWorkspace_ShouldReturnUpdated() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UpdateWorkspaceRequestDto request = new UpdateWorkspaceRequestDto("Updated Name", "Updated Desc", null);
        UpdateWorkspaceResponseDto response = UpdateWorkspaceResponseDto.builder()
                .name("Updated Name")
                .build();

        when(workspaceService.updateWorkspace(eq(workspaceId), any(UpdateWorkspaceRequestDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/workspace/{workspaceId}", workspaceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("data.name").value("Updated Name"));
    }

    @Test
    @WithMockUser
    void deleteWorkspace_ShouldReturnOk() throws Exception {
        UUID workspaceId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/workspace/{workspaceId}", workspaceId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("message").value("Workspace deleted successfully"));
    }

    @Test
    @WithMockUser
    void updateMemberRole_ShouldReturnOk() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UpdateMemberRoleRequestDto request = new UpdateMemberRoleRequestDto(UUID.randomUUID(), UpdateMemberRole.ADMIN);

        mockMvc.perform(put("/api/v1/workspace/{workspaceId}/members", workspaceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("message").value("Member role updated successfully"));
    }

    @Test
    @WithMockUser
    void removeMemberFromWorkspace_ShouldReturnOk() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/workspace/{workspaceId}/members/{userId}", workspaceId, userId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("message").value("Member removed from workspace successfully"));
    }

    @Test
    @WithMockUser
    void addMemberToWorkspace_ShouldReturnOk() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        CreateInvitationRequestDto request = new CreateInvitationRequestDto("test@example.com", WorkspaceRole.MEMBER);
        when(workspaceInvitationService.addMemberToWorkspace(any(), eq(workspaceId))).thenReturn(new InvitationResponseDto());

        mockMvc.perform(post("/api/v1/workspace/{workspaceId}", workspaceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("message").value("Member sent request successfully"));
    }

    private JsonPathResultMatchers jsonPanel(String path) {
        return jsonPath("$." + path);
    }
}
