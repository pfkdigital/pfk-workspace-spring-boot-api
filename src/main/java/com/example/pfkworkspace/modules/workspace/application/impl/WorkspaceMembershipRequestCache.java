package com.example.pfkworkspace.modules.workspace.application.impl;

import com.example.pfkworkspace.modules.auth.application.UserContextService;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceMember;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceMemberRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@RequiredArgsConstructor
public class WorkspaceMembershipRequestCache {

  private final UserContextService userContextService;
  private final WorkspaceMemberRepository workspaceMemberRepository;

  private final Map<UUID, Optional<WorkspaceMember>> cache = new HashMap<>();

  public Optional<WorkspaceMember> get(UUID workspaceId) {
    return cache.computeIfAbsent(workspaceId, this::loadMembership);
  }

  private Optional<WorkspaceMember> loadMembership(UUID workspaceId) {
    User currentUser = userContextService.getCurrentUser();
    return workspaceMemberRepository.findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId);
  }
}
