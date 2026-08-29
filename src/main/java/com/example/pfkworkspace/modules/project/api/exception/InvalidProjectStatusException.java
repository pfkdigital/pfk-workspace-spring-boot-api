package com.example.pfkworkspace.modules.project.api.exception;

import com.example.pfkworkspace.common.error.BadRequestException;

public class InvalidProjectStatusException extends BadRequestException {
    public InvalidProjectStatusException(String message) {
        super(message);
    }
}
