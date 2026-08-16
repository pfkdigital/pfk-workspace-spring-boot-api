package com.example.pfkworkspace.modules.project.api.exception;

import com.example.pfkworkspace.common.error.NotFoundException;

public class ProjectNotFoundException extends NotFoundException {
    public ProjectNotFoundException(String message) {
        super(message);
    }
}
