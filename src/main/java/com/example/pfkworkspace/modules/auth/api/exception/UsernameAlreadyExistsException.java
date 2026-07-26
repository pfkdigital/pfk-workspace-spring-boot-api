package com.example.pfkworkspace.modules.auth.api.exception;

import com.example.pfkworkspace.common.error.ConflictException;

public class UsernameAlreadyExistsException extends ConflictException {
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
