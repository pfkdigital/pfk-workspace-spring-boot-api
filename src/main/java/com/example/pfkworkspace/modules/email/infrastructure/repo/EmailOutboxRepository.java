package com.example.pfkworkspace.modules.email.infrastructure.repo;

import com.example.pfkworkspace.modules.email.domain.EmailOutbox;
import com.example.pfkworkspace.modules.email.domain.EmailOutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, UUID> {
    List<EmailOutbox> findAllByStatus(EmailOutboxStatus status);
}
