package com.example.pfkworkspace.modules.auth.api.exception;

import com.example.pfkworkspace.common.error.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
