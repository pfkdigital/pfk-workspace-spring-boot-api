package com.example.pfkworkspace.modules.workspace.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AcceptInvitationResponseDto {
    private String message;
    private WorkspaceSummaryDto workspace;
}
