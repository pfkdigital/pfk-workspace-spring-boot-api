package com.example.pfkworkspace.modules.email.application.impl;

import com.example.pfkworkspace.modules.email.api.EmailSendingException;
import com.example.pfkworkspace.modules.email.application.EmailService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final ResourceLoader resourceLoader;

    @Value("${pfk.email.resend.api-key}")
    private String apiKey;

    @Value("${pfk.email.resend.from-email}")
    private String emailFrom;

    @Override
    public void sendVerificationEmail(String to, String token) {
        try {
            String body = getHtmlBody("classpath:templates/verification-email.html")
                    .replace("{{TOKEN}}", token);
            send(to, "Verify your email", body);
        } catch (IOException | ResendException e) {
            throw new EmailSendingException("Unable to send verification email", e);
        }
    }

    @Override
    public void sendAccountVerifiedEmail(String to) {
        try {
            String body = getHtmlBody("classpath:templates/account-verified.html");
            send(to, "Your account has been verified", body);
        } catch (IOException | ResendException e) {
            throw new EmailSendingException("Unable to send account verified email", e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        try {
            String body = getHtmlBody("classpath:templates/forgot-password.html")
                    .replace("{{TOKEN}}", token);
            send(to, "Password reset request", body);
        } catch (IOException | ResendException e) {
            throw new EmailSendingException("Unable to send password reset email", e);
        }
    }

    @Override
    public void sendPasswordUpdatedEmail(String to) {
        try {
            String body = getHtmlBody("classpath:templates/reset-password-success-email.html");
            send(to, "Your password has been updated", body);
        } catch (IOException | ResendException e) {
            throw new EmailSendingException("Unable to send password updated email", e);
        }
    }

    @Override
    public void sendWorkspaceInvitationEmail(String to, Map<String, String> payload) {
        try {
            String body = getHtmlBody("classpath:templates/workspace-invitation.html")
                    .replace("{{WORKSPACE_NAME}}", payload.get("workspaceName"))
                    .replace("{{ROLE}}", payload.get("role"))
                    .replace("{{ACCEPT_URL}}", payload.get("acceptUrl"))
                    .replace("{{REJECT_URL}}", payload.get("rejectUrl"))
                    .replace("{{EXPIRY_DATE}}", payload.get("expiryDate"));
            send(to, "You are invited to " + payload.get("workspaceName") + " workspace", body);
        } catch (IOException | ResendException e) {
            throw new EmailSendingException("Unable to send workspace invitation email", e);
        }
    }

    private void send(String to, String subject, String html) throws ResendException {
        Resend resend = new Resend(apiKey);
        CreateEmailOptions options = CreateEmailOptions.builder()
                .from(emailFrom)
                .to(to)
                .subject(subject)
                .html(html)
                .build();
        resend.emails().send(options);
    }

    private String getHtmlBody(String classpath) throws IOException {
        Resource resource = resourceLoader.getResource(classpath);
        Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
        return FileCopyUtils.copyToString(reader);
    }
}
