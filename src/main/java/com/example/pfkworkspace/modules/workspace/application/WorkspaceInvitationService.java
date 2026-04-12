package com.example.pfkworkspace.modules.workspace.application;

import com.example.pfkworkspace.modules.workspace.api.dto.CreateInvitationRequestDto;
import com.example.pfkworkspace.modules.workspace.api.dto.InvitationResponseDto;

import java.util.UUID;

public interface WorkspaceInvitationService {
    InvitationResponseDto addMemberToWorkspace(CreateInvitationRequestDto createInvitationRequestDto, UUID workspaceId);
        void acceptInvitation(String token);
        void declineInvitation(String token);
        void resendInvitation(UUID invitationId);
        void revokeInvitation(UUID invitationId);
}
