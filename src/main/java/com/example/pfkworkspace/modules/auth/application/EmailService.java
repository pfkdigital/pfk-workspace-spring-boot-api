package com.example.pfkworkspace.modules.auth.application;

public interface EmailService {
    void sendVerificationEmail(String to, String subject, String token);
    void sendAccountVerifiedEmail(String to, String subject);
}
