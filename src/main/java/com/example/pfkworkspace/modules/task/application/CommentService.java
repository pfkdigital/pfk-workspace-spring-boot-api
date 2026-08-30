package com.example.pfkworkspace.modules.task.application;

import com.example.pfkworkspace.modules.task.api.dto.request.AddCommentRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.AddCommentResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.RemoveCommentResponseDto;
import java.util.UUID;

public interface CommentService {
    AddCommentResponseDto addCommentToTask(UUID workspaceId, UUID projectId, UUID taskId, AddCommentRequestDto requestDto);
    RemoveCommentResponseDto removeCommentFromTask(UUID workspaceId, UUID projectId, UUID taskId, UUID commentId);
}
