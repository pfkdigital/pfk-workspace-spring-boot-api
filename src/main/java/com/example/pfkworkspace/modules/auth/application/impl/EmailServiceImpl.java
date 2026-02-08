package com.example.pfkworkspace.modules.auth.application.impl;

import com.example.pfkworkspace.modules.auth.application.EmailService;
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
  public void sendVerificationEmail(String to, String subject, String token) {
    try {
      Resend resend = new Resend(apiKey);

      String body = getHtmlBody("classpath:templates/verification-email.html");

      CreateEmailOptions options =
          CreateEmailOptions.builder()
              .from(emailFrom)
              .to(to)
              .subject(subject)
              .html(body.replace("{{TOKEN}}", token))
              .build();

      resend.emails().send(options);
    } catch (IOException | ResendException e) {
      throw new RuntimeException("Failed to send verification email", e);
    }
  }

  @Override
  public void sendAccountVerifiedEmail(String to, String subject) {
    try {
      Resend resend = new Resend(apiKey);

      String body = getHtmlBody("classpath:templates/account-verified.html");

      CreateEmailOptions options =
          CreateEmailOptions.builder().from(emailFrom).to(to).subject(subject).html(body).build();

      resend.emails().send(options);
    } catch (IOException | ResendException e) {
      throw new RuntimeException("Failed to send account verified email", e);
    }
  }

  private String getHtmlBody(String classpath) throws IOException {
    Resource resource = resourceLoader.getResource(classpath);
    Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
    return FileCopyUtils.copyToString(reader);
  }
}
