package com.example.pfkworkspace.modules.workspace.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record CreateWorkspaceRequestDto(
        @NotBlank
        @Size(min = 3,max = 20, message = "Name must be between 3 and 20 characters.")
        String name,

        @NotBlank
        @Size(min = 3, max = 50, message = "Description must be between 3 and 50 characters")
        String description,

        @NotBlank
        @URL
        String imageUrl
) {}
