package com.example.pfkworkspace.modules.email.application.impl;

import com.example.pfkworkspace.modules.email.application.EmailService;
import com.example.pfkworkspace.modules.email.domain.EmailOutbox;
import com.example.pfkworkspace.modules.email.domain.EmailOutboxStatus;
import com.example.pfkworkspace.modules.email.infrastructure.repo.EmailOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailOutboxProcessor {

  private static final int MAX_ATTEMPTS = 3;

  private final EmailOutboxRepository emailOutboxRepository;
  private final EmailService emailService;

  @Scheduled(fixedDelay = 30000)
  public void process() {
    log.info("Scanning for pending email events");
    List<EmailOutbox> pending = emailOutboxRepository.findAllByStatus(EmailOutboxStatus.PENDING);
    for (EmailOutbox outbox : pending) {
      try {
        dispatch(outbox);
        outbox.setStatus(EmailOutboxStatus.SENT);
        log.info(
            "Email sent: id={} type={} recipient={}",
            outbox.getId(),
            outbox.getEmailType(),
            outbox.getRecipient());
      } catch (Exception e) {
        int attempts = outbox.getAttempts() + 1;
        outbox.setAttempts(attempts);
        outbox.setLastError(e.getMessage());
        if (attempts >= MAX_ATTEMPTS) {
          outbox.setStatus(EmailOutboxStatus.FAILED);
          log.error(
              "Email id={} type={} permanently failed after {} attempts: {}",
              outbox.getId(),
              outbox.getEmailType(),
              attempts,
              e.getMessage());
        } else {
          log.warn(
              "Email id={} type={} failed, attempt {}/{}: {}",
              outbox.getId(),
              outbox.getEmailType(),
              attempts,
              MAX_ATTEMPTS,
              e.getMessage());
        }
      }
      emailOutboxRepository.save(outbox);
    }
  }

  @Scheduled(fixedDelay = 60000)
  public void cleanUp() {
    // TODO - Add sent email processing at a later date
    log.info("Cleaning up sent email outbox records");
    emailOutboxRepository.deleteAllByStatus(EmailOutboxStatus.SENT);
  }

  private void dispatch(EmailOutbox outbox) {
    Map<String, String> payload = outbox.getPayload() != null ? outbox.getPayload() : Map.of();
    switch (outbox.getEmailType()) {
      case VERIFICATION ->
          emailService.sendVerificationEmail(outbox.getRecipient(), payload.get("token"));
      case ACCOUNT_VERIFIED -> emailService.sendAccountVerifiedEmail(outbox.getRecipient());
      case PASSWORD_RESET ->
          emailService.sendPasswordResetEmail(outbox.getRecipient(), payload.get("token"));
      case PASSWORD_UPDATED -> emailService.sendPasswordUpdatedEmail(outbox.getRecipient());
      case WORKSPACE_INVITATION ->
          emailService.sendWorkspaceInvitationEmail(outbox.getRecipient(), payload);
    }
  }
}
