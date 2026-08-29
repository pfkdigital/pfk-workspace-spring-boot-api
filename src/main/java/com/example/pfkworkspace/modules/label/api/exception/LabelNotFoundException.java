package com.example.pfkworkspace.modules.label.api.exception;

import com.example.pfkworkspace.common.error.NotFoundException;

public class LabelNotFoundException extends NotFoundException {
    public LabelNotFoundException(String message) {
        super(message);
    }
}
