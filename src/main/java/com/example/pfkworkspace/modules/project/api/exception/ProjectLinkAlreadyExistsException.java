package com.example.pfkworkspace.modules.project.api.exception;

import com.example.pfkworkspace.common.error.BadRequestException;

public class ProjectLinkAlreadyExistsException extends BadRequestException {
    public ProjectLinkAlreadyExistsException(String message) {
        super(message);
    }
}
