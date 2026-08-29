package com.example.pfkworkspace.modules.task.api.dto.request;

import com.example.pfkworkspace.modules.task.domain.TaskPriority;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UpdateTaskRequestDto(
        @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters.")
        String title,

        @Size(min = 3, max = 1000, message = "Description should be between 3 to 1000 characters long")
        String description,

        TaskPriority priority,

        @FutureOrPresent(message = "Due date must be in the future")
        LocalDate dueDate
) {}
