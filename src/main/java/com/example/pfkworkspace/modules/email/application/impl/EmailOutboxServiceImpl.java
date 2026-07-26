package com.example.pfkworkspace.modules.email.application.impl;

import com.example.pfkworkspace.modules.email.application.EmailOutboxService;
import com.example.pfkworkspace.modules.email.domain.EmailOutbox;
import com.example.pfkworkspace.modules.email.domain.EmailOutboxStatus;
import com.example.pfkworkspace.modules.email.domain.EmailType;
import com.example.pfkworkspace.modules.email.infrastructure.repo.EmailOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailOutboxServiceImpl implements EmailOutboxService {

    private final EmailOutboxRepository emailOutboxRepository;

    @Override
    public void queue(String recipient, EmailType type, Map<String, String> payload) {
        EmailOutbox outbox = EmailOutbox.builder()
                .recipient(recipient)
                .emailType(type)
                .payload(payload)
                .status(EmailOutboxStatus.PENDING)
                .attempts(0)
                .build();
        emailOutboxRepository.save(outbox);
    }

    @Override
    public void queue(String recipient, EmailType type) {
        queue(recipient, type, null);
    }
}
