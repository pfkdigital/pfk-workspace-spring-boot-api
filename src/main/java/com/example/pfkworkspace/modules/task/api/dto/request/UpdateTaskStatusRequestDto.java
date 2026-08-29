package com.example.pfkworkspace.modules.task.api.dto.request;

import com.example.pfkworkspace.modules.task.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequestDto(
        @NotNull(message = "Status is required")
        TaskStatus status
) {}
