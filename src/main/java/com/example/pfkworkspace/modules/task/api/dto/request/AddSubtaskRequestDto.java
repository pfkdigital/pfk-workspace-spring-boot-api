package com.example.pfkworkspace.modules.task.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddSubtaskRequestDto(
        @NotBlank
        @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters.")
        String title
) {}
