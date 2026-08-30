package com.example.pfkworkspace.modules.task.application;

import com.example.pfkworkspace.modules.task.api.dto.request.CreateTaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateTaskAssigneeRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateTaskRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.request.UpdateTaskStatusRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.CreateTaskResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.TaskDetailResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.TaskResponseDto;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    CreateTaskResponseDto createTask(CreateTaskRequestDto requestDto, UUID workspaceId, UUID projectId);
    Page<TaskResponseDto> getTasks(UUID workspaceId, UUID projectId, Pageable pageable);
    TaskDetailResponseDto getTaskDetail(UUID workspaceId, UUID projectId, UUID taskId);
    TaskDetailResponseDto updateTaskStatus(UUID workspaceId, UUID projectId, UUID taskId, UpdateTaskStatusRequestDto requestDto);
    TaskDetailResponseDto updateTaskAssignee(UUID workspaceId, UUID projectId, UUID taskId, UpdateTaskAssigneeRequestDto requestDto);
    TaskDetailResponseDto updateTask(UUID workspaceId, UUID projectId, UUID taskId, UpdateTaskRequestDto requestDto);
    void deleteTask(UUID workspaceId, UUID projectId, UUID taskId);
}
