package com.example.pfkworkspace.modules.task.application.impl;

import com.example.pfkworkspace.common.aws.service.S3Service;
import com.example.pfkworkspace.modules.task.domain.Attachment;
import com.example.pfkworkspace.modules.task.domain.AttachmentStatus;
import com.example.pfkworkspace.modules.task.infrastructure.repo.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttachmentReconciler {
    private static final Duration STALE_AFTER_DURATION = Duration.ofHours(1);
    private final S3Service s3Service;
    private final AttachmentRepository attachmentRepository;

    @Scheduled(fixedDelay = 600_000)
    @Transactional
    public void reconcile(){
        log.info("Reconciling attachments");
        List<Attachment> attachments = attachmentRepository.findAllByStatusAndCreatedAtBefore(AttachmentStatus.PENDING, Instant.ofEpochMilli(System.currentTimeMillis() - STALE_AFTER_DURATION.toMillis()));

        if (attachments.isEmpty()) {
            log.info("No attachments to reconcile");
            return;
        }

        log.info("Found {} attachments to reconcile", attachments.size());
        for(Attachment attachment : attachments) {
            if(s3Service.existsInBucket(attachment.getStorageKey())) {
                log.info("Attachment {} exists in bucket", attachment.getId());
                attachment.setStatus(AttachmentStatus.READY);
                attachmentRepository.save(attachment);
                return;
            }

            if (s3Service.existsInQuarantine(attachment.getStorageKey())) {
                log.info("Attachment {} exists in quarantine, deleting", attachment.getId());
                s3Service.deleteQuarantinedObject(attachment.getStorageKey());
                attachment.setStatus(AttachmentStatus.FAILED);
                attachmentRepository.save(attachment);
                return;
            }

            log.info("Attachment {} has no stored object; deleting row", attachment.getId());
            attachmentRepository.delete(attachment);
        }
        log.info("Finished reconciling attachments");
    }
}
