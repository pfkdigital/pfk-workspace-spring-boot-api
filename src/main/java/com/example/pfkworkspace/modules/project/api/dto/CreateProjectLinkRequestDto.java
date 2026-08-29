package com.example.pfkworkspace.modules.project.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateProjectLinkRequestDto(
        @NotBlank
        @Size(max = 100, message = "Label must be at most 100 characters")
        String label,

        @NotBlank
        @Pattern(regexp = "^https?://.+", message = "Url must be a valid http(s) URL")
        String url,

        @Pattern(
                regexp = "^pfkworkspace/projects/linkicons/.+$",
                message = "Icon must be preapproved"
        )
        String icon
) {}
