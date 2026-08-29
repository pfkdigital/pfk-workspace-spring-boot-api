package com.example.pfkworkspace.modules.project.api.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AddProjectLinkResponseDto {
    private UUID id;
    private UUID projectId;
    private String label;
    private String url;
    private String icon;
}
