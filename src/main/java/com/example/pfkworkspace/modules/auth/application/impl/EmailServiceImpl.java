package com.example.pfkworkspace.modules.auth.application.impl;

import com.example.pfkworkspace.modules.auth.application.EmailService;
import com.example.pfkworkspace.modules.auth.api.EmailSendingException;
import com.example.pfkworkspace.modules.workspace.api.dto.CreateInvitationRequestDto;
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
      Resend resend = new Resend(apiKey);

      String body = getHtmlBody("classpath:templates/verification-email.html");

      CreateEmailOptions options =
          CreateEmailOptions.builder()
              .from(emailFrom)
              .to(to)
              .subject("Verify your email")
              .html(body.replace("{{TOKEN}}", token))
              .build();

      resend.emails().send(options);
    } catch (IOException | ResendException e) {
      throw new EmailSendingException("Unable to send verification email", e);
    }
  }

  @Override
  public void sendAccountVerifiedEmail(String to) {
    try {
      Resend resend = new Resend(apiKey);

      String body = getHtmlBody("classpath:templates/account-verified.html");

      CreateEmailOptions options =
          CreateEmailOptions.builder()
              .from(emailFrom)
              .to(to)
              .subject("Your account has been verified")
              .html(body)
              .build();

      resend.emails().send(options);
    } catch (IOException | ResendException e) {
      throw new EmailSendingException("Unable to send account verified email", e);
    }
  }

  @Override
  public void sendWorkspaceInvitationEmail(
      String to, CreateInvitationRequestDto.WorkspaceInvitationParams params) {
    try {
      Resend resend = new Resend(apiKey);

      String body =
          getHtmlBody("classpath:templates/workspace-invitation.html")
              .replace("{{WORKSPACE_NAME}}", params.getWorkspaceName())
              .replace("{{ROLE}}", params.getWorkspaceRole().name())
              .replace("{{ACCEPT_URL}}", params.getAcceptUrl())
              .replace("{{REJECT_URL}}", params.getRejectUrl())
              .replace("{{EXPIRY_DATE}}", params.getExpiryDate());

      CreateEmailOptions options =
          CreateEmailOptions.builder()
              .from(emailFrom)
              .to(to)
              .subject("You are invited to " + params.getWorkspaceName() + " workspace")
              .html(body)
              .build();

      resend.emails().send(options);
    } catch (IOException | ResendException e) {
      throw new EmailSendingException("Unable to send workspace invitation email", e);
    }
  }

  public void sendPasswordResetEmail(String to, String token) {
    try {
      Resend resend = new Resend(apiKey);

      String body = getHtmlBody("classpath:templates/forgot-password.html");

      CreateEmailOptions options =
          CreateEmailOptions.builder()
              .from(emailFrom)
              .to(to)
              .subject("Password reset request")
              .html(body.replace("{{TOKEN}}", token))
              .build();

      resend.emails().send(options);
    } catch (IOException | ResendException e) {
      throw new EmailSendingException("Unable to send password reset email", e);
    }
  }

  public void sendPasswordUpdatedEmail(String to) {
    try {
      Resend resend = new Resend(apiKey);

      String body = getHtmlBody("classpath:templates/reset-password-success-email.html");

      CreateEmailOptions options =
          CreateEmailOptions.builder()
              .from(emailFrom)
              .to(to)
              .subject("Your password has been updated")
              .html(body)
              .build();

      resend.emails().send(options);
    } catch (IOException | ResendException e) {
      throw new EmailSendingException("Unable to send password updated email", e);
    }
  }

  private String getHtmlBody(String classpath) throws IOException {
    Resource resource = resourceLoader.getResource(classpath);
    Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
    return FileCopyUtils.copyToString(reader);
  }
}
