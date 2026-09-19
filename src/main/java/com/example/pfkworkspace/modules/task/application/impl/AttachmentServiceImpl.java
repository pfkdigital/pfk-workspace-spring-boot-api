package com.example.pfkworkspace.modules.task.application.impl;

import com.example.pfkworkspace.modules.task.application.AttachmentService;
import com.example.pfkworkspace.modules.task.application.messaging.ScanResultMessage;
import com.example.pfkworkspace.modules.task.domain.Attachment;
import com.example.pfkworkspace.modules.task.domain.AttachmentStatus;
import com.example.pfkworkspace.modules.task.infrastructure.repo.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentServiceImpl implements AttachmentService {
    private final AttachmentRepository attachmentRepository;

    @Override
    @Transactional
    public void applyScanResult(ScanResultMessage scanResult) {
        Attachment attachment = attachmentRepository.findByStorageKey(scanResult.storageKey()).orElse(null);
        if (attachment == null) {
            log.warn("Attachment not found for storage key: {}", scanResult.storageKey());
            return;
        }
        
        AttachmentStatus current = attachment.getStatus();
        if (current == AttachmentStatus.READY || current == AttachmentStatus.FAILED) {
            log.debug("Ignoring scan result {} for attachment {} already {}", scanResult.verdict(), attachment.getId(), current);
            return;
        }

        AttachmentStatus next = switch (scanResult.verdict()) {
            case CLEAN -> AttachmentStatus.READY;
            case INFECTED, FAILED -> AttachmentStatus.FAILED;
        };

        if (next == AttachmentStatus.FAILED) {
            log.warn("Attachment scan {}: {} ({})", scanResult.verdict(), scanResult.storageKey(), scanResult.reason());
        } else {
            log.info("Attachment scan passed: {}", scanResult.storageKey());
        }
        attachment.setStatus(next);
        attachmentRepository.save(attachment);
    }
}
