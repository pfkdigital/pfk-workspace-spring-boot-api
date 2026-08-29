package com.example.pfkworkspace.modules.task.api.dto.request;

import java.util.UUID;

public record UpdateTaskAssigneeRequestDto(
        UUID assigneeId
) {}
