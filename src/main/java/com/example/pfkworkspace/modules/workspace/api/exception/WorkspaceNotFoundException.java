package com.example.pfkworkspace.modules.workspace.api.exception;

import com.example.pfkworkspace.common.error.NotFoundException;

public class WorkspaceNotFoundException extends NotFoundException {
    public WorkspaceNotFoundException(String message) {
        super(message);
    }
}
