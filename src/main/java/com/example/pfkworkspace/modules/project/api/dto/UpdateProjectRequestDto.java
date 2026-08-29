package com.example.pfkworkspace.modules.project.api.dto;

import com.example.pfkworkspace.modules.project.domain.ProjectStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateProjectRequestDto(
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters.")
        String name,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a hex code, e.g. #10B981")
        String color,

        ProjectStatus status,

        LocalDate startDate,

        LocalDate targetDate
) {}
