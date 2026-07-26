package com.example.pfkworkspace.modules.user.api;

import com.example.pfkworkspace.common.error.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
