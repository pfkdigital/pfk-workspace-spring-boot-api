package com.example.pfkworkspace.modules.email.application.impl;

import com.example.pfkworkspace.modules.email.domain.EmailOutbox;
import com.example.pfkworkspace.modules.email.domain.EmailOutboxStatus;
import com.example.pfkworkspace.modules.email.domain.EmailType;
import com.example.pfkworkspace.modules.email.infrastructure.repo.EmailOutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailOutboxServiceImplTest {

    @Mock
    private EmailOutboxRepository emailOutboxRepository;

    @InjectMocks
    private EmailOutboxServiceImpl emailOutboxService;

    @Test
    void queue_WithPayload_ShouldSaveRecordWithPendingStatusAndZeroAttempts() {
        Map<String, String> payload = Map.of("token", "abc123");

        emailOutboxService.queue("user@example.com", EmailType.VERIFICATION, payload);

        ArgumentCaptor<EmailOutbox> captor = ArgumentCaptor.forClass(EmailOutbox.class);
        verify(emailOutboxRepository).save(captor.capture());

        EmailOutbox saved = captor.getValue();
        assertThat(saved.getRecipient()).isEqualTo("user@example.com");
        assertThat(saved.getEmailType()).isEqualTo(EmailType.VERIFICATION);
        assertThat(saved.getPayload()).containsEntry("token", "abc123");
        assertThat(saved.getStatus()).isEqualTo(EmailOutboxStatus.PENDING);
        assertThat(saved.getAttempts()).isEqualTo(0);
    }

    @Test
    void queue_WithoutPayload_ShouldSaveRecordWithNullPayload() {
        emailOutboxService.queue("user@example.com", EmailType.ACCOUNT_VERIFIED);

        ArgumentCaptor<EmailOutbox> captor = ArgumentCaptor.forClass(EmailOutbox.class);
        verify(emailOutboxRepository).save(captor.capture());

        EmailOutbox saved = captor.getValue();
        assertThat(saved.getRecipient()).isEqualTo("user@example.com");
        assertThat(saved.getEmailType()).isEqualTo(EmailType.ACCOUNT_VERIFIED);
        assertThat(saved.getPayload()).isNull();
        assertThat(saved.getStatus()).isEqualTo(EmailOutboxStatus.PENDING);
        assertThat(saved.getAttempts()).isEqualTo(0);
    }
}
