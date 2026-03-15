package com.example.pfkworkspace.modules.workspace.api.dto;

import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class WorkspaceDetailDto {
    private UUID id;
    private String name;
    private String description;
    private String imageUrl;
    private OwnerDto owner;
    private List<MemberDto> members;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OwnerDto {
        private UUID id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberDto {
        private UUID id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private WorkspaceRole role;
        private Instant joinedAt;
    }
}
