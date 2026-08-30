package com.example.pfkworkspace.modules.task.api.exception;

import com.example.pfkworkspace.common.error.NotFoundException;

public class CommentNotFoundException extends NotFoundException {
    public CommentNotFoundException(String message) {
        super(message);
    }
}
