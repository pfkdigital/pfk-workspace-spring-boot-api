package com.example.pfkworkspace.modules.task.api.exception;

import com.example.pfkworkspace.common.error.NotFoundException;

public class AttachmentNotFoundException extends NotFoundException {
    public AttachmentNotFoundException(String message) {
        super(message);
    }
}
