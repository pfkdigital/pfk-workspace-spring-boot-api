package com.example.pfkworkspace.modules.task.api.exception;

import com.example.pfkworkspace.common.error.NotFoundException;

public class SubtaskNotFoundException extends NotFoundException {
    public SubtaskNotFoundException(String message) {
        super(message);
    }
}
