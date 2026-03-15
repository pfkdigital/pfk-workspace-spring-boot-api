package com.example.pfkworkspace.modules.workspace.application.impl;

import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.user.infrastructure.repo.UserRepository;
import com.example.pfkworkspace.modules.workspace.api.dto.CreateWorkspaceRequestDto;
import com.example.pfkworkspace.modules.workspace.api.dto.CreateWorkspaceResponseDto;
import com.example.pfkworkspace.modules.workspace.application.WorkspaceService;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceMember;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

  private final UserRepository userRepository;
  private final WorkspaceRepository workspaceRepository;

  @Override
  @Transactional
  public CreateWorkspaceResponseDto createWorkspace(
      CreateWorkspaceRequestDto createWorkspaceRequestDto) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User owner =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () ->
                    new UsernameNotFoundException(
                        "User with email address " + email + " has not been found"));

    Workspace newWorkspace =
        workspaceRepository.save(
            Workspace.builder()
                .name(createWorkspaceRequestDto.name())
                .description(createWorkspaceRequestDto.description())
                .imageUrl(null)
                .owner(owner)
                .build());

    WorkspaceMember workspaceMember =
        WorkspaceMember.builder()
            .user(owner)
            .role(WorkspaceRole.OWNER)
            .joinedAt(Instant.now())
            .build();

    newWorkspace.addWorkspaceMember(workspaceMember);

    return CreateWorkspaceResponseDto.builder()
        .id(newWorkspace.getId())
        .name(newWorkspace.getName())
        .description(newWorkspace.getDescription())
        .imageUrl(newWorkspace.getImageUrl())
        .createdAt(newWorkspace.getCreatedAt())
        .build();
  }
}
