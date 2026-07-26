package com.example.pfkworkspace.modules.email.application;

import java.util.Map;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
    void sendAccountVerifiedEmail(String to);
    void sendPasswordResetEmail(String to, String token);
    void sendPasswordUpdatedEmail(String to);
    void sendWorkspaceInvitationEmail(String to, Map<String, String> payload);
}
