package com.example.pfkworkspace.modules.task.infrastructure.repo;

import com.example.pfkworkspace.modules.task.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    Optional<Attachment> findByStorageKey(String storageKey);
}
