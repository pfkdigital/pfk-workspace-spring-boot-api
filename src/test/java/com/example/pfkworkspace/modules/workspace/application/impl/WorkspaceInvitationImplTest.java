package com.example.pfkworkspace.modules.workspace.application.impl;

import com.example.pfkworkspace.common.security.HashingService;
import com.example.pfkworkspace.modules.auth.application.UserContextService;
import com.example.pfkworkspace.modules.email.application.EmailOutboxService;
import com.example.pfkworkspace.modules.email.domain.EmailType;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.user.infrastructure.repo.UserRepository;
import com.example.pfkworkspace.modules.workspace.api.dto.CreateInvitationRequestDto;
import com.example.pfkworkspace.modules.workspace.api.dto.InvitationResponseDto;
import com.example.pfkworkspace.modules.workspace.api.exception.WorkspaceInvitationNotValidException;
import com.example.pfkworkspace.modules.workspace.application.WorkspaceSecurityService;
import com.example.pfkworkspace.modules.workspace.application.mapper.WorkspaceInvitationMapper;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceInvitation;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceInvitationRepository;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceInvitationImplTest {

    @Mock
    private EmailOutboxService emailOutboxService;
    @Mock
    private HashingService hashingService;
    @Mock
    private WorkspaceInvitationMapper workspaceInvitationMapper;
    @Mock
    private WorkspaceSecurityService workspaceSecurityService;
    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private WorkspaceInvitationRepository workspaceInvitationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserContextService userContextService;

    @InjectMocks
    private WorkspaceInvitationImpl workspaceInvitationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(workspaceInvitationService, "baseUrl", "http://localhost:8080");
    }

    @Test
    void addMemberToWorkspace_WhenAuthorized_ShouldCreateInvitation() {
        UUID workspaceId = UUID.randomUUID();
        CreateInvitationRequestDto request = new CreateInvitationRequestDto("test@example.com", WorkspaceRole.MEMBER);
        Workspace workspace = Workspace.builder()
                .name("Test Workspace")
                .workspaceInvitations(new ArrayList<>())
                .build();
        User user = new User();
        user.setEmail("test@example.com");

        when(workspaceSecurityService.isOwnerOrAdmin(workspaceId)).thenReturn(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(hashingService.sha256Hash(anyString())).thenReturn("hashed-token");
        when(workspaceInvitationMapper.toInvitationResponseDto(any())).thenReturn(new InvitationResponseDto());

        InvitationResponseDto response = workspaceInvitationService.addMemberToWorkspace(request, workspaceId);

        assertThat(response).isNotNull();
        verify(workspaceInvitationRepository).save(any(WorkspaceInvitation.class));
        verify(emailOutboxService).queue(eq("test@example.com"), eq(EmailType.WORKSPACE_INVITATION), any());
    }

    @Test
    void acceptInvitation_WhenValid_ShouldWork() {
        String token = "valid-token";
        String hashedToken = "hashed-token";
        User currentUser = new User();
        currentUser.setEmail("test@example.com");
        Workspace workspace = new Workspace();
        workspace.setName("Test Workspace");

        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .email("test@example.com")
                .tokenHash(hashedToken)
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .isUsed(false)
                .workspace(workspace)
                .role(WorkspaceRole.MEMBER)
                .build();

        when(hashingService.sha256Hash(token)).thenReturn(hashedToken);
        when(workspaceInvitationRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(invitation));
        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(currentUser));

        workspaceInvitationService.acceptInvitation(token);

        assertThat(invitation.getIsUsed()).isTrue();
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void acceptInvitation_WhenExpired_ShouldThrowException() {
        String token = "expired-token";
        String hashedToken = "hashed-token";
        User currentUser = new User();
        currentUser.setEmail("test@example.com");

        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .email("test@example.com")
                .tokenHash(hashedToken)
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .isUsed(false)
                .build();

        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(hashingService.sha256Hash(token)).thenReturn(hashedToken);
        when(workspaceInvitationRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> workspaceInvitationService.acceptInvitation(token))
                .isInstanceOf(WorkspaceInvitationNotValidException.class);
    }

    @Test
    void revokeInvitation_WhenAuthorized_ShouldRevoke() {
        UUID invitationId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        Workspace workspace = new Workspace();
        workspace.setId(workspaceId);
        workspace.setName("Test Workspace");
        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .workspace(workspace)
                .isUsed(false)
                .build();

        when(workspaceInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(workspaceSecurityService.isOwnerOrAdmin(workspaceId)).thenReturn(true);

        workspaceInvitationService.revokeInvitation(invitationId);

        assertThat(invitation.getIsUsed()).isTrue();
        verify(workspaceInvitationRepository).save(invitation);
    }
}
