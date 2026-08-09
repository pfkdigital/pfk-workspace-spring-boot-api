package com.example.pfkworkspace.modules.workspace.application.mapper;

import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.workspace.api.dto.WorkspaceDetailDto;
import com.example.pfkworkspace.modules.workspace.api.dto.WorkspaceSummaryDto;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceMember;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceMapper {

    public WorkspaceSummaryDto toSummaryDto(Workspace workspace) {
        return WorkspaceSummaryDto.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .imageUrl(workspace.getImageUrl())
                .memberCount(workspace.getWorkspaceMembers().size())
                .createdAt(workspace.getCreatedAt())
                .build();
    }

    public WorkspaceDetailDto toDetailDto(Workspace workspace) {
        return WorkspaceDetailDto.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .imageUrl(workspace.getImageUrl())
                .owner(toOwnerDto(workspace.getOwner()))
                .members(workspace.getWorkspaceMembers().stream()
                        .map(this::toMemberDto)
                        .toList())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }

    private WorkspaceDetailDto.OwnerDto toOwnerDto(User owner) {
        return WorkspaceDetailDto.OwnerDto.builder()
                .id(owner.getId())
                .username(owner.getUsername())
                .email(owner.getEmail())
                .firstName(owner.getFirstName())
                .lastName(owner.getLastName())
                .build();
    }

    private WorkspaceDetailDto.MemberDto toMemberDto(WorkspaceMember member) {
        return WorkspaceDetailDto.MemberDto.builder()
                .id(member.getUser().getId())
                .username(member.getUser().getUsername())
                .email(member.getUser().getEmail())
                .firstName(member.getUser().getFirstName())
                .lastName(member.getUser().getLastName())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
