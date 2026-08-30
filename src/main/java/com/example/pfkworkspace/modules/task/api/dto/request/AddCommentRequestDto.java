package com.example.pfkworkspace.modules.task.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCommentRequestDto(
        @NotBlank
        @Size(min = 1, max = 2000, message = "Body must be between 1 and 2000 characters.")
        String body
) {}
