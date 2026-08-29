package com.example.pfkworkspace.modules.workspace.application.impl;

import com.example.pfkworkspace.TestcontainersConfiguration;
import com.example.pfkworkspace.config.DataSeeder;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.workspace.api.dto.UpdateWorkspaceRequestDto;
import com.example.pfkworkspace.modules.workspace.api.dto.UpdateWorkspaceResponseDto;
import com.example.pfkworkspace.modules.workspace.api.dto.WorkspaceDetailDto;
import com.example.pfkworkspace.modules.workspace.application.WorkspaceService;
import com.example.pfkworkspace.modules.workspace.application.mapper.WorkspaceMapper;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceMember;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceMemberRepository;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class WorkspaceServiceImplSecurityTest {

    @Autowired
    private WorkspaceService workspaceService;

    @MockitoBean
    private WorkspaceRepository workspaceRepository;

    @MockitoBean
    private WorkspaceMemberRepository workspaceMemberRepository;

    @MockitoBean
    private WorkspaceMapper workspaceMapper;

    @MockitoBean
    private DataSeeder dataSeeder;

    private User currentUser;
    private UUID workspaceId;
    private Workspace workspace;

    @BeforeEach
    void setUp() {

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest()));

        currentUser =
                User.builder()
                        .email("member@example.com")
                        .username("member")
                        .firstName("Member")
                        .lastName("User")
                        .build();
        currentUser.setId(UUID.randomUUID());

        workspaceId = UUID.randomUUID();
        workspace = Workspace.builder().name("Test Workspace").owner(currentUser).build();
        workspace.setId(workspaceId);

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                currentUser, null, currentUser.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void updateWorkspace_WhenCallerIsNotOwner_ShouldBeDeniedByPreAuthorize() {
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId))
                .thenReturn(Optional.of(WorkspaceMember.builder().role(WorkspaceRole.MEMBER).build()));

        UpdateWorkspaceRequestDto request = new UpdateWorkspaceRequestDto("New Name", "New Desc", null);

        assertThatThrownBy(() -> workspaceService.updateWorkspace(workspaceId, request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateWorkspace_WhenCallerIsOwner_ShouldBeAllowedByPreAuthorize() {
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId))
                .thenReturn(Optional.of(WorkspaceMember.builder().role(WorkspaceRole.OWNER).build()));
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(workspace)).thenReturn(workspace);

        UpdateWorkspaceRequestDto request = new UpdateWorkspaceRequestDto("New Name", "New Desc", null);

        UpdateWorkspaceResponseDto response = workspaceService.updateWorkspace(workspaceId, request);

        assertThat(response.getName()).isEqualTo("New Name");
    }

    @Test
    void getWorkspaceDetail_WhenCallerIsNotMember_ShouldBeDeniedByPreAuthorize() {
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceService.getWorkspaceDetail(workspaceId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getWorkspaceDetail_WhenCallerIsMember_ShouldBeAllowedByPreAuthorize() {
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId))
                .thenReturn(Optional.of(WorkspaceMember.builder().role(WorkspaceRole.MEMBER).build()));
        when(workspaceRepository.findByIdWithDetails(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceMapper.toDetailDto(workspace))
                .thenReturn(WorkspaceDetailDto.builder().id(workspaceId).name("Test Workspace").build());

        WorkspaceDetailDto result = workspaceService.getWorkspaceDetail(workspaceId);

        assertThat(result.getName()).isEqualTo("Test Workspace");
    }
}
