package com.example.pfkworkspace.modules.workspace.application.impl;

import com.example.pfkworkspace.modules.workspace.domain.WorkspaceMember;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceSecurityServiceImplTest {

    @Mock
    private WorkspaceMembershipRequestCache membershipCache;

    @InjectMocks
    private WorkspaceSecurityServiceImpl workspaceSecurityService;

    private UUID workspaceId;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
    }

    @Test
    void isOwner_WhenIsOwner_ShouldReturnTrue() {
        WorkspaceMember member = WorkspaceMember.builder().role(WorkspaceRole.OWNER).build();
        when(membershipCache.get(workspaceId)).thenReturn(Optional.of(member));

        boolean result = workspaceSecurityService.isOwner(workspaceId);

        assertThat(result).isTrue();
    }

    @Test
    void isAdmin_WhenIsAdmin_ShouldReturnTrue() {
        WorkspaceMember member = WorkspaceMember.builder().role(WorkspaceRole.ADMIN).build();
        when(membershipCache.get(workspaceId)).thenReturn(Optional.of(member));

        boolean result = workspaceSecurityService.isAdmin(workspaceId);

        assertThat(result).isTrue();
    }

    @Test
    void isMember_WhenIsMember_ShouldReturnTrue() {
        WorkspaceMember member = new WorkspaceMember();
        when(membershipCache.get(workspaceId)).thenReturn(Optional.of(member));

        boolean result = workspaceSecurityService.isMember(workspaceId);

        assertThat(result).isTrue();
    }

    @Test
    void isMember_WhenNotMember_ShouldReturnFalse() {
        when(membershipCache.get(workspaceId)).thenReturn(Optional.empty());

        boolean result = workspaceSecurityService.isMember(workspaceId);

        assertThat(result).isFalse();
    }

    @Test
    void isOwnerOrAdmin_WhenIsAdmin_ShouldReturnTrue() {
        WorkspaceMember member = WorkspaceMember.builder().role(WorkspaceRole.ADMIN).build();
        when(membershipCache.get(workspaceId)).thenReturn(Optional.of(member));

        boolean result = workspaceSecurityService.isOwnerOrAdmin(workspaceId);

        assertThat(result).isTrue();
    }

    @Test
    void isOwnerOrAdmin_WhenIsMemberOnly_ShouldReturnFalse() {
        WorkspaceMember member = WorkspaceMember.builder().role(WorkspaceRole.MEMBER).build();
        when(membershipCache.get(workspaceId)).thenReturn(Optional.of(member));

        boolean result = workspaceSecurityService.isOwnerOrAdmin(workspaceId);

        assertThat(result).isFalse();
    }
}
