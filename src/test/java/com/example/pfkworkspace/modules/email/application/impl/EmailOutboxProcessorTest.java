package com.example.pfkworkspace.modules.email.application.impl;

import com.example.pfkworkspace.modules.email.application.EmailService;
import com.example.pfkworkspace.modules.email.domain.EmailOutbox;
import com.example.pfkworkspace.modules.email.domain.EmailOutboxStatus;
import com.example.pfkworkspace.modules.email.domain.EmailType;
import com.example.pfkworkspace.modules.email.infrastructure.repo.EmailOutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static com.example.pfkworkspace.modules.email.domain.EmailOutboxStatus.FAILED;
import static com.example.pfkworkspace.modules.email.domain.EmailOutboxStatus.PENDING;
import static com.example.pfkworkspace.modules.email.domain.EmailOutboxStatus.SENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailOutboxProcessorTest {

    @Mock
    private EmailOutboxRepository emailOutboxRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailOutboxProcessor processor;

    @Test
    void process_WhenNoPendingEmails_ShouldDoNothing() {
        when(emailOutboxRepository.findAllByStatus(PENDING)).thenReturn(List.of());

        processor.process();

        verify(emailOutboxRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void process_WhenVerificationEmail_ShouldDispatchTokenAndMarkSent() {
        EmailOutbox outbox = pendingOutbox(EmailType.VERIFICATION, Map.of("token", "abc123"));
        when(emailOutboxRepository.findAllByStatus(PENDING)).thenReturn(List.of(outbox));

        processor.process();

        verify(emailService).sendVerificationEmail("user@example.com", "abc123");
        assertThat(outbox.getStatus()).isEqualTo(SENT);
        verify(emailOutboxRepository).save(outbox);
    }

    @Test
    void process_WhenAccountVerifiedEmail_ShouldDispatchAndMarkSent() {
        EmailOutbox outbox = pendingOutbox(EmailType.ACCOUNT_VERIFIED, null);
        when(emailOutboxRepository.findAllByStatus(PENDING)).thenReturn(List.of(outbox));

        processor.process();

        verify(emailService).sendAccountVerifiedEmail("user@example.com");
        assertThat(outbox.getStatus()).isEqualTo(SENT);
    }

    @Test
    void process_WhenPasswordResetEmail_ShouldDispatchTokenAndMarkSent() {
        EmailOutbox outbox = pendingOutbox(EmailType.PASSWORD_RESET, Map.of("token", "reset-token"));
        when(emailOutboxRepository.findAllByStatus(PENDING)).thenReturn(List.of(outbox));

        processor.process();

        verify(emailService).sendPasswordResetEmail("user@example.com", "reset-token");
        assertThat(outbox.getStatus()).isEqualTo(SENT);
    }

    @Test
    void process_WhenPasswordUpdatedEmail_ShouldDispatchAndMarkSent() {
        EmailOutbox outbox = pendingOutbox(EmailType.PASSWORD_UPDATED, null);
        when(emailOutboxRepository.findAllByStatus(PENDING)).thenReturn(List.of(outbox));

        processor.process();

        verify(emailService).sendPasswordUpdatedEmail("user@example.com");
        assertThat(outbox.getStatus()).isEqualTo(SENT);
    }

    @Test
    void process_WhenWorkspaceInvitationEmail_ShouldDispatchPayloadAndMarkSent() {
        Map<String, String> payload = Map.of(
                "workspaceName", "Acme",
                "role", "MEMBER",
                "acceptUrl", "http://example.com/accept",
                "rejectUrl", "http://example.com/reject",
                "expiryDate", "2026-08-01"
        );
        EmailOutbox outbox = pendingOutbox(EmailType.WORKSPACE_INVITATION, payload);
        when(emailOutboxRepository.findAllByStatus(PENDING)).thenReturn(List.of(outbox));

        processor.process();

        verify(emailService).sendWorkspaceInvitationEmail("user@example.com", payload);
        assertThat(outbox.getStatus()).isEqualTo(SENT);
    }

    @Test
    void process_WhenDispatchFails_ShouldIncrementAttemptsAndRemainPending() {
        EmailOutbox outbox = pendingOutbox(EmailType.VERIFICATION, Map.of("token", "abc123"));
        when(emailOutboxRepository.findAllByStatus(PENDING)).thenReturn(List.of(outbox));
        doThrow(new RuntimeException("SMTP unavailable")).when(emailService).sendVerificationEmail(any(), any());

        processor.process();

        assertThat(outbox.getStatus()).isEqualTo(PENDING);
        assertThat(outbox.getAttempts()).isEqualTo(1);
        assertThat(outbox.getLastError()).isEqualTo("SMTP unavailable");
        verify(emailOutboxRepository).save(outbox);
    }

    @Test
    void process_WhenMaxAttemptsReached_ShouldMarkAsFailed() {
        EmailOutbox outbox = pendingOutbox(EmailType.VERIFICATION, Map.of("token", "abc123"));
        outbox.setAttempts(2);
        when(emailOutboxRepository.findAllByStatus(PENDING)).thenReturn(List.of(outbox));
        doThrow(new RuntimeException("SMTP unavailable")).when(emailService).sendVerificationEmail(any(), any());

        processor.process();

        assertThat(outbox.getStatus()).isEqualTo(FAILED);
        assertThat(outbox.getAttempts()).isEqualTo(3);
        assertThat(outbox.getLastError()).isEqualTo("SMTP unavailable");
    }

    @Test
    void process_WhenOneEmailFails_ShouldContinueProcessingRemaining() {
        EmailOutbox failing = pendingOutbox(EmailType.VERIFICATION, Map.of("token", "abc123"));
        EmailOutbox succeeding = pendingOutbox(EmailType.ACCOUNT_VERIFIED, null);
        when(emailOutboxRepository.findAllByStatus(PENDING)).thenReturn(List.of(failing, succeeding));
        doThrow(new RuntimeException("SMTP unavailable")).when(emailService).sendVerificationEmail(any(), any());

        processor.process();

        assertThat(failing.getStatus()).isEqualTo(PENDING);
        assertThat(succeeding.getStatus()).isEqualTo(SENT);
        verify(emailOutboxRepository, times(2)).save(any());
    }

    private EmailOutbox pendingOutbox(EmailType type, Map<String, String> payload) {
        return EmailOutbox.builder()
                .recipient("user@example.com")
                .emailType(type)
                .payload(payload)
                .status(EmailOutboxStatus.PENDING)
                .attempts(0)
                .build();
    }
}
