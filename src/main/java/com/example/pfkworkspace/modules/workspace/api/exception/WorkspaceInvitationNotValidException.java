package com.example.pfkworkspace.modules.workspace.api.exception;

import com.example.pfkworkspace.common.error.BadRequestException;

public class WorkspaceInvitationNotValidException extends BadRequestException {
    public WorkspaceInvitationNotValidException(String message) {
        super(message);
    }
}
