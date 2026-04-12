package com.example.pfkworkspace.modules.workspace.api.dto;

import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public record CreateInvitationRequestDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotNull(message = "Role is required")
        WorkspaceRole role
) {
        @AllArgsConstructor
        @NoArgsConstructor
        @Getter
        @Setter
        @Builder
        public static class WorkspaceInvitationParams {
            private String workspaceName;
            private WorkspaceRole workspaceRole;
            private String acceptUrl;
            private String rejectUrl;
            private String expiryDate;
        }
}