package com.example.pfkworkspace.modules.email.application;

import com.example.pfkworkspace.modules.email.domain.EmailType;

import java.util.Map;

public interface EmailOutboxService {
    void queue(String recipient, EmailType type, Map<String, String> payload);
    void queue(String recipient, EmailType type);
}
