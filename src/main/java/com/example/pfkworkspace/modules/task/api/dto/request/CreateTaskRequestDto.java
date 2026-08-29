package com.example.pfkworkspace.modules.task.api.dto.request;

import com.example.pfkworkspace.modules.task.domain.TaskPriority;
import com.example.pfkworkspace.modules.task.domain.TaskStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record CreateTaskRequestDto(
        @NotBlank
        @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters.")
        String title,
        @NotBlank
        @Size(min = 3, max = 1000, message = "Description should be between 3 to 1000 characters long")
        String description,
        @NotNull(message = "Status is required")
        TaskStatus taskStatus,
        @NotNull(message = "Priority is required")
        TaskPriority taskPriority,
        @NotNull(message = "Assigned user is required")
        UUID assignedTo,
        @NotNull(message = "Due date is required")
        @FutureOrPresent(message = "Due date must be in the future")
        LocalDate dueDate,
        Set<UUID> labelIds
){}