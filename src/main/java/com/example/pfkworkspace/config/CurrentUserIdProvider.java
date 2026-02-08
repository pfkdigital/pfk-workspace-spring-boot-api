package com.example.pfkworkspace.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserIdProvider {

    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return null;
        }

        Object principal = auth.getPrincipal();

        // Option A: principal is a UUID string
        if (principal instanceof String s) {
            try {
                return UUID.fromString(s);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        // Option B: your custom principal exposes getId()
        if (principal instanceof HasUserId hasUserId) {
            return hasUserId.getUserId();
        }

        return null;
    }

    public interface HasUserId {
        UUID getUserId();
    }
}

