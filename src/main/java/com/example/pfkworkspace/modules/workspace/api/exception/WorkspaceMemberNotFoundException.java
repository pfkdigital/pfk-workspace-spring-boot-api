package com.example.pfkworkspace.modules.workspace.api.exception;

import com.example.pfkworkspace.common.error.NotFoundException;

public class WorkspaceMemberNotFoundException extends NotFoundException {
    public WorkspaceMemberNotFoundException(String message) {
        super(message);
    }
}
