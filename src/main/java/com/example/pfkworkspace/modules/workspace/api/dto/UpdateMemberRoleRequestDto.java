package com.example.pfkworkspace.modules.workspace.api.dto;

import com.example.pfkworkspace.modules.workspace.domain.UpdateMemberRole;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

public record UpdateMemberRoleRequestDto(
    @NotNull(message = "Member ID is required") UUID memberId,
    @NotNull(message = "Role is required") UpdateMemberRole role) {}
