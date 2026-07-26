package com.example.pfkworkspace.modules.workspace.api.exception;

import com.example.pfkworkspace.common.error.NotFoundException;

public class WorkspaceInvitationNotFoundException extends NotFoundException {
    public WorkspaceInvitationNotFoundException(String message) {
        super(message);
    }
}
