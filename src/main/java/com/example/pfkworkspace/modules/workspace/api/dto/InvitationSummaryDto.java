package com.example.pfkworkspace.modules.workspace.api.dto;

import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InvitationSummaryDto {
    private UUID id;
    private String email;
    private WorkspaceRole role;
    private Instant expiresAt;
    private Boolean isUsed;
    private Instant createdAt;
}
