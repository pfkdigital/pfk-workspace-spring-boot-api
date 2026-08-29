package com.example.pfkworkspace.modules.project.api.exception;

import com.example.pfkworkspace.common.error.NotFoundException;

public class ProjectLinkNotFoundException extends NotFoundException {
    public ProjectLinkNotFoundException(String message) {
        super(message);
    }
}
