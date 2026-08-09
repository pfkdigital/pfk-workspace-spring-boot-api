package com.example.pfkworkspace.modules.workspace.application.impl;

import com.example.pfkworkspace.common.security.HashingService;
import com.example.pfkworkspace.common.util.DateFormatter;
import com.example.pfkworkspace.common.util.RandomTokenGenerator;
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
import com.example.pfkworkspace.modules.workspace.application.WorkspaceInvitationService;
import com.example.pfkworkspace.modules.workspace.application.WorkspaceSecurityService;
import com.example.pfkworkspace.modules.workspace.application.mapper.WorkspaceInvitationMapper;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceInvitation;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceMember;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceInvitationRepository;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceInvitationServiceImpl implements WorkspaceInvitationService {

  private final EmailOutboxService emailOutboxService;
  private final HashingService hashingService;
  private final WorkspaceInvitationMapper workspaceInvitationMapper;
  private final WorkspaceSecurityService workspaceSecurityService;
  private final WorkspaceRepository workspaceRepository;
  private final WorkspaceInvitationRepository workspaceInvitationRepository;
  private final UserRepository userRepository;
  private final UserContextService userContextService;

  @Value("${pfk.app.base-url}")
  private String baseUrl;

  @Override
  @Transactional
  public InvitationResponseDto addMemberToWorkspace(
      CreateInvitationRequestDto createInvitationRequestDto, UUID workspaceId) {
    if (!workspaceSecurityService.isOwnerOrAdmin(workspaceId)) {
      throw new AuthorizationDeniedException(
          "You must be a user or admin of the workspace to add a new member");
    }

    User derivedUser =
        userRepository
            .findByEmail(createInvitationRequestDto.email())
            .orElseThrow(
                () ->
                    new UserNotFoundException(
                        "User not found with the email " + createInvitationRequestDto.email()));

    if (workspaceInvitationRepository.existsByEmailAndWorkspace_IdAndIsUsedFalseAndExpiresAtAfter(
        derivedUser.getEmail(), workspaceId, Instant.now())) {
      throw new WorkspaceInvitationNotValidException(
          "A pending invitation already exists for this email");
    }

    Workspace selectedWorkspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(
                () ->
                    new WorkspaceNotFoundException(
                        "Workspace not found with the id: " + workspaceId));

    String token = RandomTokenGenerator.generateToken();
    Instant expiryDate = Instant.now().plus(Duration.ofDays(7));

    WorkspaceInvitation workspaceInvitation =
        WorkspaceInvitation.builder()
            .email(derivedUser.getEmail())
            .role(createInvitationRequestDto.role())
            .tokenHash(hashingService.sha256Hash(token))
            .expiresAt(expiryDate)
            .isUsed(false)
            .build();

    selectedWorkspace.addWorkspaceInvitation(workspaceInvitation);
    workspaceInvitationRepository.save(workspaceInvitation);

    emailOutboxService.queue(
        derivedUser.getEmail(),
        EmailType.WORKSPACE_INVITATION,
        buildInvitationPayload(
            workspaceId,
            selectedWorkspace.getName(),
            createInvitationRequestDto.role(),
            expiryDate,
            token));

    log.info(
        "Invitation sent to email={} for workspaceId={} with role={}",
        derivedUser.getEmail(),
        workspaceId,
        createInvitationRequestDto.role());
    return workspaceInvitationMapper.toInvitationResponseDto(workspaceInvitation);
  }

  @Override
  @Transactional
  public void acceptInvitation(String token) {
    WorkspaceInvitation workspaceInvitation = getAndValidateInvitationForCurrentUser(token);
    Workspace workspace = workspaceInvitation.getWorkspace();
    String newUserEmail = workspaceInvitation.getEmail();
    User newUser =
        userRepository
            .findByEmail(newUserEmail)
            .orElseThrow(
                () -> new UserNotFoundException("User not found with email: " + newUserEmail));

    workspace.addWorkspaceMember(
        WorkspaceMember.builder()
            .user(newUser)
            .joinedAt(Instant.now())
            .role(workspaceInvitation.getRole())
            .build());
    workspaceInvitation.setIsUsed(true);
    workspaceRepository.save(workspace);

    log.info("userId={} accepted invitation to workspaceId={}", newUser.getId(), workspace.getId());
  }

  @Override
  @Transactional
  public void declineInvitation(String token) {
    WorkspaceInvitation workspaceInvitation = getAndValidateInvitationForCurrentUser(token);

    workspaceInvitation.setIsUsed(true);
    workspaceInvitationRepository.save(workspaceInvitation);

    log.info("Invitation declined to workspaceId={}", workspaceInvitation.getWorkspace().getId());
  }

  @Override
  @Transactional
  public void resendInvitation(UUID invitationId) {
    WorkspaceInvitation workspaceInvitation =
        workspaceInvitationRepository
            .findById(invitationId)
            .orElseThrow(
                () ->
                    new WorkspaceInvitationNotFoundException(
                        "Workspace Invitation not found for id: " + invitationId));

    if (!workspaceSecurityService.isOwnerOrAdmin(workspaceInvitation.getWorkspace().getId())) {
      throw new AuthorizationDeniedException(
          "You must be an owner or admin of the workspace to resend an invitation");
    }

    if (workspaceInvitation.getIsUsed()
        || workspaceInvitation.getExpiresAt().isAfter(Instant.now())) {
      throw new WorkspaceInvitationNotValidException("Workspace Invitation is not valid");
    }

    String token = RandomTokenGenerator.generateToken();
    Instant expiryDate = Instant.now().plus(Duration.ofDays(7));

    workspaceInvitation.setTokenHash(hashingService.sha256Hash(token));
    workspaceInvitation.setExpiresAt(expiryDate);
    workspaceInvitationRepository.save(workspaceInvitation);

    emailOutboxService.queue(
        workspaceInvitation.getEmail(),
        EmailType.WORKSPACE_INVITATION,
        buildInvitationPayload(
            workspaceInvitation.getWorkspace().getId(),
            workspaceInvitation.getWorkspace().getName(),
            workspaceInvitation.getRole(),
            expiryDate,
            token));

    log.info(
        "Invitation id={} resent to email={} for workspaceId={}",
        invitationId,
        workspaceInvitation.getEmail(),
        workspaceInvitation.getWorkspace().getId());
  }

  @Override
  @Transactional
  public void revokeInvitation(UUID invitationId) {
    WorkspaceInvitation workspaceInvitation =
        workspaceInvitationRepository
            .findById(invitationId)
            .orElseThrow(
                () ->
                    new WorkspaceInvitationNotFoundException(
                        "Workspace Invitation not found for id: " + invitationId));

    if (!workspaceSecurityService.isOwnerOrAdmin(workspaceInvitation.getWorkspace().getId())) {
      throw new AuthorizationDeniedException(
          "You must be an owner or admin of the workspace to revoke an invitation");
    }

    if (workspaceInvitation.getIsUsed()) {
      throw new WorkspaceInvitationNotValidException(
          "Workspace Invitation has already been used, cannot revoke it");
    }

    workspaceInvitation.setIsUsed(true);
    workspaceInvitationRepository.save(workspaceInvitation);

    log.info(
        "Invitation id={} revoked for workspaceId={}",
        invitationId,
        workspaceInvitation.getWorkspace().getId());
  }

  private Map<String, String> buildInvitationPayload(
      UUID workspaceId,
      String workspaceName,
      WorkspaceRole role,
      Instant expiryDate,
      String token) {
    return Map.of(
        "workspaceName",
        workspaceName,
        "role",
        role.name(),
        "acceptUrl",
        baseUrl + "/workspaces/" + workspaceId + "/invitations/accept?token=" + token,
        "rejectUrl",
        baseUrl + "/workspaces/" + workspaceId + "/invitations/reject?token=" + token,
        "expiryDate",
        DateFormatter.formatDate(expiryDate));
  }

  private WorkspaceInvitation getAndValidateInvitationForCurrentUser(String token) {
    User currentUser = userContextService.getCurrentUser();
    String tokenHash = hashingService.sha256Hash(token);
    WorkspaceInvitation workspaceInvitation =
        workspaceInvitationRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(
                () -> new WorkspaceInvitationNotFoundException("Invitation not found or invalid"));

    if (currentUser.getEmail() == null
        || !currentUser.getEmail().equals(workspaceInvitation.getEmail())) {
      throw new AuthorizationDeniedException(
          "You are not authorized to accept this invitation, please use the email that the invitation was sent to");
    }

    if (workspaceInvitation.getIsUsed()
        || workspaceInvitation.getExpiresAt().isBefore(Instant.now())) {
      throw new WorkspaceInvitationNotValidException(
          "Workspace is invalid, please request another");
    }

    return workspaceInvitation;
  }
}
