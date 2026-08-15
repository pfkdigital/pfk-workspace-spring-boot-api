package com.example.pfkworkspace.modules.workspace.api.dto;

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
public class CreateWorkspaceResponseDto {
    private UUID id;
    private String name;
    private String description;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;
}