package com.example.pfkworkspace.modules.workspace.application.impl;

import com.example.pfkworkspace.common.security.HashingService;
import com.example.pfkworkspace.modules.auth.application.UserContextService;
import com.example.pfkworkspace.modules.email.application.EmailOutboxService;
import com.example.pfkworkspace.modules.email.domain.EmailType;
import com.example.pfkworkspace.modules.user.api.exception.UserNotFoundException;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.user.infrastructure.repo.UserRepository;
import com.example.pfkworkspace.modules.workspace.api.dto.CreateInvitationRequestDto;
import com.example.pfkworkspace.modules.workspace.api.dto.InvitationResponseDto;
import com.example.pfkworkspace.modules.workspace.api.exception.WorkspaceInvitationNotFoundException;
import com.example.pfkworkspace.modules.workspace.api.exception.WorkspaceInvitationNotValidException;
import com.example.pfkworkspace.modules.workspace.api.exception.WorkspaceNotFoundException;
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
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceInvitationServiceImplTest {

    @Mock private EmailOutboxService emailOutboxService;
    @Mock private HashingService hashingService;
    @Mock private WorkspaceInvitationMapper workspaceInvitationMapper;
    @Mock private WorkspaceSecurityService workspaceSecurityService;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private WorkspaceInvitationRepository workspaceInvitationRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserContextService userContextService;

    @InjectMocks
    private WorkspaceInvitationServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");
    }

    @Test
    void addMemberToWorkspace_WhenValid_ShouldCreateInvitationAndQueueEmail() {
        UUID workspaceId = UUID.randomUUID();
        CreateInvitationRequestDto request = new CreateInvitationRequestDto("user@example.com", WorkspaceRole.MEMBER);
        User user = userWithEmail("user@example.com");
        Workspace workspace = workspace("Test Workspace");

        when(workspaceSecurityService.isOwnerOrAdmin(workspaceId)).thenReturn(true);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(workspaceInvitationRepository.existsByEmailAndWorkspace_IdAndIsUsedFalseAndExpiresAtAfter(
                eq("user@example.com"), eq(workspaceId), any(Instant.class))).thenReturn(false);
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(hashingService.sha256Hash(anyString())).thenReturn("hashed-token");
        when(workspaceInvitationMapper.toInvitationResponseDto(any())).thenReturn(new InvitationResponseDto());

        InvitationResponseDto response = service.addMemberToWorkspace(request, workspaceId);

        assertThat(response).isNotNull();
        verify(workspaceInvitationRepository).save(any(WorkspaceInvitation.class));
        verify(emailOutboxService).queue(eq("user@example.com"), eq(EmailType.WORKSPACE_INVITATION), any());
    }

    @Test
    void addMemberToWorkspace_WhenNotAuthorized_ShouldThrow() {
        UUID workspaceId = UUID.randomUUID();
        when(workspaceSecurityService.isOwnerOrAdmin(workspaceId)).thenReturn(false);

        assertThatThrownBy(() -> service.addMemberToWorkspace(
                new CreateInvitationRequestDto("user@example.com", WorkspaceRole.MEMBER), workspaceId))
                .isInstanceOf(AuthorizationDeniedException.class);

        verifyNoInteractions(userRepository, workspaceRepository, emailOutboxService);
    }

    @Test
    void addMemberToWorkspace_WhenUserNotFound_ShouldThrow() {
        UUID workspaceId = UUID.randomUUID();
        when(workspaceSecurityService.isOwnerOrAdmin(workspaceId)).thenReturn(true);
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addMemberToWorkspace(
                new CreateInvitationRequestDto("unknown@example.com", WorkspaceRole.MEMBER), workspaceId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void addMemberToWorkspace_WhenPendingInvitationExists_ShouldThrow() {
        UUID workspaceId = UUID.randomUUID();
        User user = userWithEmail("user@example.com");

        when(workspaceSecurityService.isOwnerOrAdmin(workspaceId)).thenReturn(true);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(workspaceInvitationRepository.existsByEmailAndWorkspace_IdAndIsUsedFalseAndExpiresAtAfter(
                eq("user@example.com"), eq(workspaceId), any(Instant.class))).thenReturn(true);

        assertThatThrownBy(() -> service.addMemberToWorkspace(
                new CreateInvitationRequestDto("user@example.com", WorkspaceRole.MEMBER), workspaceId))
                .isInstanceOf(WorkspaceInvitationNotValidException.class)
                .hasMessageContaining("pending invitation already exists");
    }

    @Test
    void addMemberToWorkspace_WhenWorkspaceNotFound_ShouldThrow() {
        UUID workspaceId = UUID.randomUUID();
        User user = userWithEmail("user@example.com");

        when(workspaceSecurityService.isOwnerOrAdmin(workspaceId)).thenReturn(true);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(workspaceInvitationRepository.existsByEmailAndWorkspace_IdAndIsUsedFalseAndExpiresAtAfter(
                eq("user@example.com"), eq(workspaceId), any(Instant.class))).thenReturn(false);
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addMemberToWorkspace(
                new CreateInvitationRequestDto("user@example.com", WorkspaceRole.MEMBER), workspaceId))
                .isInstanceOf(WorkspaceNotFoundException.class);
    }

    @Test
    void acceptInvitation_WhenValid_ShouldAddMemberAndMarkUsed() {
        String token = "valid-token";
        User currentUser = userWithEmail("user@example.com");
        Workspace workspace = workspace("Test Workspace");
        WorkspaceInvitation invitation = validInvitation("user@example.com", workspace);

        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(hashingService.sha256Hash(token)).thenReturn("hashed-token");
        when(workspaceInvitationRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(invitation));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(currentUser));

        service.acceptInvitation(token);

        assertThat(invitation.getIsUsed()).isTrue();
        assertThat(workspace.getWorkspaceMembers()).hasSize(1);
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void acceptInvitation_WhenTokenNotFound_ShouldThrow() {
        String token = "bad-token";
        when(userContextService.getCurrentUser()).thenReturn(userWithEmail("user@example.com"));
        when(hashingService.sha256Hash(token)).thenReturn("hashed-token");
        when(workspaceInvitationRepository.findByTokenHash("hashed-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.acceptInvitation(token))
                .isInstanceOf(WorkspaceInvitationNotFoundException.class);
    }

    @Test
    void acceptInvitation_WhenWrongUser_ShouldThrow() {
        String token = "valid-token";
        User currentUser = userWithEmail("other@example.com");
        WorkspaceInvitation invitation = validInvitation("user@example.com", workspace("Test"));

        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(hashingService.sha256Hash(token)).thenReturn("hashed-token");
        when(workspaceInvitationRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.acceptInvitation(token))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    void acceptInvitation_WhenExpired_ShouldThrow() {
        String token = "expired-token";
        User currentUser = userWithEmail("user@example.com");
        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .email("user@example.com")
                .tokenHash("hashed-token")
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .isUsed(false)
                .workspace(workspace("Test"))
                .role(WorkspaceRole.MEMBER)
                .build();

        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(hashingService.sha256Hash(token)).thenReturn("hashed-token");
        when(workspaceInvitationRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.acceptInvitation(token))
                .isInstanceOf(WorkspaceInvitationNotValidException.class);
    }

    @Test
    void acceptInvitation_WhenAlreadyUsed_ShouldThrow() {
        String token = "used-token";
        User currentUser = userWithEmail("user@example.com");
        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .email("user@example.com")
                .tokenHash("hashed-token")
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .isUsed(true)
                .workspace(workspace("Test"))
                .role(WorkspaceRole.MEMBER)
                .build();

        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(hashingService.sha256Hash(token)).thenReturn("hashed-token");
        when(workspaceInvitationRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.acceptInvitation(token))
                .isInstanceOf(WorkspaceInvitationNotValidException.class);
    }


    @Test
    void declineInvitation_WhenValid_ShouldMarkUsed() {
        String token = "valid-token";
        User currentUser = userWithEmail("user@example.com");
        Workspace workspace = workspace("Test Workspace");
        WorkspaceInvitation invitation = validInvitation("user@example.com", workspace);

        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(hashingService.sha256Hash(token)).thenReturn("hashed-token");
        when(workspaceInvitationRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(invitation));

        service.declineInvitation(token);

        assertThat(invitation.getIsUsed()).isTrue();
        verify(workspaceInvitationRepository).save(invitation);
    }

    @Test
    void declineInvitation_WhenWrongUser_ShouldThrow() {
        String token = "valid-token";
        User currentUser = userWithEmail("other@example.com");
        WorkspaceInvitation invitation = validInvitation("user@example.com", workspace("Test"));

        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(hashingService.sha256Hash(token)).thenReturn("hashed-token");
        when(workspaceInvitationRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.declineInvitation(token))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    void declineInvitation_WhenExpired_ShouldThrow() {
        String token = "expired-token";
        User currentUser = userWithEmail("user@example.com");
        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .email("user@example.com")
                .tokenHash("hashed-token")
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .isUsed(false)
                .workspace(workspace("Test"))
                .role(WorkspaceRole.MEMBER)
                .build();

        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(hashingService.sha256Hash(token)).thenReturn("hashed-token");
        when(workspaceInvitationRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.declineInvitation(token))
                .isInstanceOf(WorkspaceInvitationNotValidException.class);
    }


    @Test
    void resendInvitation_WhenExpiredAndNotUsed_ShouldRegenerateAndQueueEmail() {
        UUID invitationId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        Workspace workspace = workspaceWithId("Test Workspace", workspaceId);
        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .email("user@example.com")
                .tokenHash("old-hash")
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .isUsed(false)
                .workspace(workspace)
                .role(WorkspaceRole.MEMBER)
                .build();

        when(workspaceInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(workspaceSecurityService.isOwnerOrAdmin(workspaceId)).thenReturn(true);
        when(hashingService.sha256Hash(anyString())).thenReturn("new-hash");

        service.resendInvitation(invitationId);

        assertThat(invitation.getTokenHash()).isEqualTo("new-hash");
        assertThat(invitation.getExpiresAt()).isAfter(Instant.now());
        verify(workspaceInvitationRepository).save(invitation);
        verify(emailOutboxService).queue(eq("user@example.com"), eq(EmailType.WORKSPACE_INVITATION), any());
    }

    @Test
    void resendInvitation_WhenNotFound_ShouldThrow() {
        UUID invitationId = UUID.randomUUID();
        when(workspaceInvitationRepository.findById(invitationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resendInvitation(invitationId))
                .isInstanceOf(WorkspaceInvitationNotFoundException.class);
    }

    @Test
    void resendInvitation_WhenNotAuthorized_ShouldThrow() {
        UUID invitationId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .email("user@example.com")
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .isUsed(false)
                .workspace(workspaceWithId("Test", workspaceId))
                .role(WorkspaceRole.MEMBER)
                .build();

        when(workspaceInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(workspaceSecurityService.isOwnerOrAdmin(workspaceId)).thenReturn(false);

        assertThatThrownBy(() -> service.resendInvitation(invitationId))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    void resendInvitation_WhenStillValid_ShouldThrow() {
        UUID invitationId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .email("user@example.com")
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .isUsed(false)
                .workspace(workspaceWithId("Test", workspaceId))
                .role(WorkspaceRole.MEMBER)
                .build();

        when(workspaceInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(workspaceSecurityService.isOwnerOrAdmin(workspaceId)).thenReturn(true);

        assertThatThrownBy(() -> service.resendInvitation(invitationId))
                .isInstanceOf(WorkspaceInvitationNotValidException.class);
    }

    @Test
    void resendInvitation_WhenAlreadyUsed_ShouldThrow() {
        UUID invitationId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .email("user@example.com")
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .isUsed(true)
                .workspace(workspaceWithId("Test", workspaceId))
                .role(WorkspaceRole.MEMBER)
                .build();

        when(workspaceInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(workspaceSecurityService.isOwnerOrAdmin(workspaceId)).thenReturn(true);

        assertThatThrownBy(() -> service.resendInvitation(invitationId))
                .isInstanceOf(WorkspaceInvitationNotValidException.class);
    }

    @Test
    void revokeInvitation_WhenAuthorizedAndNotUsed_ShouldMarkUsed() {
        UUID invitationId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .workspace(workspaceWithId("Test", workspaceId))
                .isUsed(false)
                .build();

        when(workspaceInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(workspaceSecurityService.isOwnerOrAdmin(workspaceId)).thenReturn(true);

        service.revokeInvitation(invitationId);

        assertThat(invitation.getIsUsed()).isTrue();
        verify(workspaceInvitationRepository).save(invitation);
    }

    @Test
    void revokeInvitation_WhenNotFound_ShouldThrow() {
        UUID invitationId = UUID.randomUUID();
        when(workspaceInvitationRepository.findById(invitationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.revokeInvitation(invitationId))
                .isInstanceOf(WorkspaceInvitationNotFoundException.class);
    }

    @Test
    void revokeInvitation_WhenNotAuthorized_ShouldThrow() {
        UUID invitationId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .workspace(workspaceWithId("Test", workspaceId))
                .isUsed(false)
                .build();

        when(workspaceInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(workspaceSecurityService.isOwnerOrAdmin(workspaceId)).thenReturn(false);

        assertThatThrownBy(() -> service.revokeInvitation(invitationId))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    void revokeInvitation_WhenAlreadyUsed_ShouldThrow() {
        UUID invitationId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .workspace(workspaceWithId("Test", workspaceId))
                .isUsed(true)
                .build();

        when(workspaceInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(workspaceSecurityService.isOwnerOrAdmin(workspaceId)).thenReturn(true);

        assertThatThrownBy(() -> service.revokeInvitation(invitationId))
                .isInstanceOf(WorkspaceInvitationNotValidException.class);
    }

    private User userWithEmail(String email) {
        User user = new User();
        user.setEmail(email);
        return user;
    }

    private Workspace workspace(String name) {
        return Workspace.builder().name(name).build();
    }

    private Workspace workspaceWithId(String name, UUID id) {
        Workspace workspace = Workspace.builder().name(name).build();
        workspace.setId(id);
        return workspace;
    }

    private WorkspaceInvitation validInvitation(String email, Workspace workspace) {
        return WorkspaceInvitation.builder()
                .email(email)
                .tokenHash("hashed-token")
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .isUsed(false)
                .workspace(workspace)
                .role(WorkspaceRole.MEMBER)
                .build();
    }
}
