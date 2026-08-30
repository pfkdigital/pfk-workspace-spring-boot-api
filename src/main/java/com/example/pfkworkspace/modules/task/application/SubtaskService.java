package com.example.pfkworkspace.modules.task.application;

import com.example.pfkworkspace.modules.task.api.dto.request.AddSubtaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateSubtaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.RemoveSubtaskResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.SubtaskResponseDto;
import java.util.UUID;

public interface SubtaskService {
    SubtaskResponseDto addSubtask(UUID workspaceId, UUID projectId, UUID taskId, AddSubtaskRequestDto requestDto);
    RemoveSubtaskResponseDto removeSubtask(UUID workspaceId, UUID projectId, UUID taskId, UUID subtaskId);
    SubtaskResponseDto updateSubtask(UUID workspaceId, UUID projectId, UUID taskId, UUID subtaskId, UpdateSubtaskRequestDto requestDto);
}
