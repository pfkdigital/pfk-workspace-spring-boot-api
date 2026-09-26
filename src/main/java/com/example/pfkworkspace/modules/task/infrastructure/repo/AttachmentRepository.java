package com.example.pfkworkspace.modules.task.infrastructure.repo;

import com.example.pfkworkspace.modules.task.domain.Attachment;
import com.example.pfkworkspace.modules.task.domain.AttachmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    Optional<Attachment> findByStorageKey(String storageKey);
    Optional<Attachment> findByIdAndTaskId(UUID attachmentId, UUID taskId);
    List<Attachment> findAllByStatusAndCreatedAtBefore(AttachmentStatus status, Instant cutOff);
}
