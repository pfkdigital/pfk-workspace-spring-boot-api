package com.example.pfkworkspace.modules.workspace.application.mapper;

import com.example.pfkworkspace.modules.workspace.api.dto.InvitationResponseDto;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceInvitation;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceInvitationMapper {

  public InvitationResponseDto toInvitationResponseDto(WorkspaceInvitation invitation) {
    return InvitationResponseDto.builder()
        .id(invitation.getId())
        .email(invitation.getEmail())
        .role(invitation.getRole())
        .token(invitation.getTokenHash())
        .expiresAt(invitation.getExpiresAt())
        .createdAt(invitation.getCreatedAt())
        .build();
  }
}
