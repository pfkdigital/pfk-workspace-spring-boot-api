package com.example.pfkworkspace.modules.task.api.exception;

import com.example.pfkworkspace.common.error.NotFoundException;

public class TaskNotFoundException extends NotFoundException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}
