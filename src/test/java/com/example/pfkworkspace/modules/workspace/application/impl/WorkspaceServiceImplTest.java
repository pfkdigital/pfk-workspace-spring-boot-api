package com.example.pfkworkspace.modules.workspace.application.impl;

import com.example.pfkworkspace.modules.auth.application.UserContextService;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.user.infrastructure.repo.UserRepository;
import com.example.pfkworkspace.modules.workspace.api.dto.*;
import com.example.pfkworkspace.modules.workspace.api.exception.WorkspaceMemberNotFoundException;
import com.example.pfkworkspace.modules.workspace.api.exception.WorkspaceNotFoundException;
import com.example.pfkworkspace.modules.workspace.application.WorkspaceSecurityService;
import com.example.pfkworkspace.modules.workspace.application.mapper.WorkspaceMapper;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceMember;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceMemberRepository;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserContextService userContextService;
    @Mock
    private WorkspaceMapper workspaceMapper;
    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;
    @Mock
    private WorkspaceSecurityService workspaceSecurityService;

    @InjectMocks
    private WorkspaceServiceImpl workspaceService;

    private User currentUser;
    private UUID workspaceId;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .email("owner@example.com")
                .username("owner")
                .firstName("Owner")
                .lastName("User")
                .build();
        currentUser.setId(UUID.randomUUID());
        workspaceId = UUID.randomUUID();
        workspace = Workspace.builder()
                .name("Test Workspace")
                .owner(currentUser)
                .workspaceMembers(new ArrayList<>())
                .build();
        workspace.setId(workspaceId);
    }

    @Test
    void createWorkspace_ShouldCreateSuccessfully() {
        CreateWorkspaceRequestDto request = new CreateWorkspaceRequestDto("New Workspace", "Description", "http://image.url");
        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(invocation -> {
            Workspace w = invocation.getArgument(0);
            w.setId(UUID.randomUUID());
            if (w.getWorkspaceMembers() == null) {
                w.setWorkspaceMembers(new ArrayList<>());
            }
            return w;
        });

        CreateWorkspaceResponseDto response = workspaceService.createWorkspace(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("New Workspace");
        verify(workspaceRepository).save(any(Workspace.class));
    }

    @Test
    void updateWorkspace_WhenOwner_ShouldUpdateSuccessfully() {
        UpdateWorkspaceRequestDto request = new UpdateWorkspaceRequestDto("Updated Name", "Updated Desc", "http://image.url");
        when(workspaceSecurityService.isOwner(workspaceId)).thenReturn(true);
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        UpdateWorkspaceResponseDto response = workspaceService.updateWorkspace(workspaceId, request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Name");
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void updateWorkspace_WhenNotOwner_ShouldThrowException() {
        UpdateWorkspaceRequestDto request = new UpdateWorkspaceRequestDto("Updated Name", "Updated Desc", null);
        when(workspaceSecurityService.isOwner(workspaceId)).thenReturn(false);

        assertThatThrownBy(() -> workspaceService.updateWorkspace(workspaceId, request))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    void getWorkspaces_ShouldReturnList() {
        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(workspaceMemberRepository.findWorkspacesByUserId(currentUser.getId())).thenReturn(List.of(workspace));
        when(workspaceMapper.toSummaryDto(any())).thenReturn(WorkspaceSummaryDto.builder().id(workspaceId).name("Test Workspace").build());

        List<WorkspaceSummaryDto> results = workspaceService.getWorkspaces();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Test Workspace");
    }

    @Test
    void getWorkspaceDetail_WhenMember_ShouldReturnDetail() {
        when(workspaceSecurityService.isMember(workspaceId)).thenReturn(true);
        when(workspaceRepository.findByIdWithDetails(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceMapper.toDetailDto(workspace)).thenReturn(WorkspaceDetailDto.builder().id(workspaceId).name("Test Workspace").build());

        WorkspaceDetailDto result = workspaceService.getWorkspaceDetail(workspaceId);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Workspace");
    }

    @Test
    void deleteWorkspace_WhenOwner_ShouldDelete() {
        when(workspaceSecurityService.isOwner(workspaceId)).thenReturn(true);
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));

        workspaceService.deleteWorkspace(workspaceId);

        verify(workspaceRepository).delete(workspace);
    }

    @Test
    void removeUserFromWorkspace_WhenOwnerOrAdmin_ShouldRemove() {
        UUID userIdToRemove = UUID.randomUUID();
        WorkspaceMember memberToRemove = new WorkspaceMember();
        memberToRemove.setId(userIdToRemove);
        
        when(workspaceSecurityService.isOwnerOrAdmin(workspaceId)).thenReturn(true);
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(userIdToRemove, workspaceId)).thenReturn(Optional.of(memberToRemove));

        workspaceService.removeUserFromWorkspace(workspaceId, userIdToRemove);

        verify(workspaceRepository).save(workspace);
    }

    @Test
    void updateMemberRole_WhenAuthorized_ShouldUpdate() {
        UUID userId = UUID.randomUUID();

        WorkspaceMember currentMember = new WorkspaceMember();
        currentMember.setRole(WorkspaceRole.ADMIN);

        WorkspaceMember targetMember = new WorkspaceMember();
        targetMember.setRole(WorkspaceRole.MEMBER);

        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId)).thenReturn(Optional.of(currentMember));
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(userId, workspaceId)).thenReturn(Optional.of(targetMember));

        workspaceService.updateMemberRole(workspaceId, userId, WorkspaceRole.ADMIN);

        assertThat(targetMember.getRole()).isEqualTo(WorkspaceRole.ADMIN);
        verify(workspaceMemberRepository).save(targetMember);
    }

    @Test
    void updateMemberRole_ToOwnerByNonOwner_ShouldThrowException() {
        UUID userId = UUID.randomUUID();

        WorkspaceMember currentMember = new WorkspaceMember();
        currentMember.setRole(WorkspaceRole.ADMIN);

        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId)).thenReturn(Optional.of(currentMember));

        assertThatThrownBy(() -> workspaceService.updateMemberRole(workspaceId, userId, WorkspaceRole.OWNER))
                .isInstanceOf(AuthorizationDeniedException.class);
    }
}
