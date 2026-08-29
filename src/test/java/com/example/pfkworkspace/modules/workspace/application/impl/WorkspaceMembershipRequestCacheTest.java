package com.example.pfkworkspace.modules.workspace.application.impl;

import com.example.pfkworkspace.modules.auth.application.UserContextService;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceMember;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceMembershipRequestCacheTest {

    @Mock
    private UserContextService userContextService;
    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @InjectMocks
    private WorkspaceMembershipRequestCache cache;

    private User currentUser;
    private UUID workspaceId;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        workspaceId = UUID.randomUUID();
    }

    @Test
    void get_WhenCalledTwiceForSameWorkspace_ShouldHitRepositoryOnlyOnce() {
        WorkspaceMember member = new WorkspaceMember();
        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId))
                .thenReturn(Optional.of(member));

        Optional<WorkspaceMember> first = cache.get(workspaceId);
        Optional<WorkspaceMember> second = cache.get(workspaceId);

        assertThat(first).contains(member);
        assertThat(second).contains(member);
        verify(workspaceMemberRepository, times(1))
                .findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId);
    }

    @Test
    void get_WhenNotAMember_ShouldCacheEmptyResult() {
        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId))
                .thenReturn(Optional.empty());

        Optional<WorkspaceMember> first = cache.get(workspaceId);
        Optional<WorkspaceMember> second = cache.get(workspaceId);

        assertThat(first).isEmpty();
        assertThat(second).isEmpty();
        verify(workspaceMemberRepository, times(1))
                .findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId);
    }

    @Test
    void get_WhenCalledForDifferentWorkspaces_ShouldHitRepositoryForEach() {
        UUID otherWorkspaceId = UUID.randomUUID();
        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId))
                .thenReturn(Optional.of(new WorkspaceMember()));
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(currentUser.getId(), otherWorkspaceId))
                .thenReturn(Optional.empty());

        cache.get(workspaceId);
        cache.get(otherWorkspaceId);

        verify(workspaceMemberRepository, times(1))
                .findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId);
        verify(workspaceMemberRepository, times(1))
                .findByUserIdAndWorkspaceId(currentUser.getId(), otherWorkspaceId);
    }
}
