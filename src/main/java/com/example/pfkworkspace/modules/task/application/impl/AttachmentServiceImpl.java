package com.example.pfkworkspace.modules.task.application.impl;

import com.example.pfkworkspace.common.aws.service.S3Service;
import com.example.pfkworkspace.common.error.ConflictException;
import com.example.pfkworkspace.modules.auth.application.UserContextService;
import com.example.pfkworkspace.modules.task.api.dto.request.CreateAttachmentRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.CreateAttachmentResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.GetAttachmentResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.RemoveAttachmentResponseDto;
import com.example.pfkworkspace.modules.task.api.exception.AttachmentNotFoundException;
import com.example.pfkworkspace.modules.task.application.AttachmentService;
import com.example.pfkworkspace.modules.task.application.TaskAccessService;
import com.example.pfkworkspace.modules.task.application.messaging.ScanResultMessage;
import com.example.pfkworkspace.modules.task.application.messaging.ScanVerdict;
import com.example.pfkworkspace.modules.task.domain.Attachment;
import com.example.pfkworkspace.modules.task.domain.AttachmentStatus;
import com.example.pfkworkspace.modules.task.domain.Task;
import com.example.pfkworkspace.modules.task.infrastructure.repo.AttachmentRepository;
import com.example.pfkworkspace.modules.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentServiceImpl implements AttachmentService {
  private static final Duration URL_EXPIRY = Duration.ofMinutes(15);

  private final AttachmentRepository attachmentRepository;
  private final TaskAccessService taskAccessService;
  private final UserContextService userContextService;
  private final S3Service s3Service;

  private static String buildStorageKey(
      UUID workspaceId, UUID projectId, UUID taskId, String extension) {
    return "workspaces/%s/projects/%s/tasks/%s/attachments/%s.%s"
        .formatted(workspaceId, projectId, taskId, UUID.randomUUID(), extension);
  }

  @Override
  @Transactional
  @PreAuthorize("@workspaceSecurity.isMember(#workspaceId)")
  public CreateAttachmentResponseDto createAttachment(
      @P("workspaceId") UUID workspaceId,
      UUID projectId,
      UUID taskId,
      CreateAttachmentRequestDto request) {
    Task task = taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId);
    User currentUser = userContextService.getCurrentUser();

    String extension = request.extension().toLowerCase(Locale.ROOT);
    String checksum = request.checksum().toLowerCase(Locale.ROOT);
    String storageKey = buildStorageKey(workspaceId, projectId, taskId, extension);

    Attachment attachment =
        Attachment.builder()
            .project(task.getProject())
            .workspace(task.getWorkspace())
            .filename(request.filename())
            .contentType(request.contentType())
            .extension(extension)
            .sizeBytes(request.sizeBytes())
            .checksum(checksum)
            .storageKey(storageKey)
            .status(AttachmentStatus.PENDING)
            .uploadedBy(currentUser)
            .build();

    task.addAttachment(attachment);
    attachmentRepository.save(attachment);

    String preSignedUrl =
        s3Service.generateUploadUrl(
            storageKey, request.contentType(), request.sizeBytes(), checksum, URL_EXPIRY);
    log.info(
        "Created attachment {} for task {} with storage key {}",
        attachment.getId(),
        taskId,
        storageKey);

    return CreateAttachmentResponseDto.builder()
        .filename(attachment.getFilename())
        .preSignedUrl(preSignedUrl)
        .status(attachment.getStatus())
        .build();
  }

  @Override
  @PreAuthorize("@workspaceSecurity.isMember(#workspaceId)")
  @Transactional(readOnly = true)
  public GetAttachmentResponseDto getAttachment(
      UUID workspaceId, UUID projectId, UUID taskId, UUID attachmentId) {
    Task task = taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId);
    Attachment attachment =
        attachmentRepository
            .findByIdAndTaskId(attachmentId, task.getId())
            .orElseThrow(
                () ->
                    new AttachmentNotFoundException(
                        "Attachment not found with id " + attachmentId));

    if (attachment.getStatus() != AttachmentStatus.READY) {
      throw new ConflictException("Attachment not ready with id " + attachmentId);
    }

    return GetAttachmentResponseDto.builder()
        .presignedUrl(
            s3Service.generateDownloadUrl(
                attachment.getStorageKey(),
                attachment.getContentType(),
                attachment.getFilename(),
                URL_EXPIRY))
        .attachmentId(attachment.getId())
        .fileName(attachment.getFilename())
        .contentType(attachment.getContentType())
        .attachmentSize(attachment.getSizeBytes())
        .attachmentId(attachmentId)
        .build();
  }

  @Override
  @Transactional
  @PreAuthorize("@workspaceSecurity.isOwnerOrAdmin(#workspaceId)")
  public RemoveAttachmentResponseDto deleteAttachment(
      UUID workspaceId, UUID projectId, UUID taskId, UUID attachmentId) {
    Task task = taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId);

    Attachment attachment =
        attachmentRepository
            .findByIdAndTaskId(attachmentId, taskId)
            .orElseThrow(
                () ->
                    new AttachmentNotFoundException(
                        "Attachment not found with id " + attachmentId));

    task.removeAttachment(attachment);
    attachmentRepository.delete(attachment);

    if (attachment.getStatus() == AttachmentStatus.READY) {
      s3Service.deleteObject(attachment.getStorageKey());
    } else {
      s3Service.deleteQuarantinedObject(attachment.getStorageKey());
    }

    return RemoveAttachmentResponseDto.builder()
        .attachmentId(attachment.getId())
        .taskId(task.getId())
        .build();
  }

  @Override
  @Transactional
  public void applyScanResult(ScanResultMessage scanResult) {
    Attachment attachment =
        attachmentRepository.findByStorageKey(scanResult.storageKey()).orElse(null);
    if (attachment == null) {
      log.warn("Attachment not found for storage key: {}", scanResult.storageKey());
      if (scanResult.verdict() == ScanVerdict.CLEAN) {
        log.warn("Deleting orphaned object for missing attachment: {}", scanResult.storageKey());
        s3Service.deleteObject(scanResult.storageKey());
      }
      return;
    }

    AttachmentStatus current = attachment.getStatus();
    if (current == AttachmentStatus.READY || current == AttachmentStatus.FAILED) {
      log.debug(
          "Ignoring scan result {} for attachment {} already {}",
          scanResult.verdict(),
          attachment.getId(),
          current);
      return;
    }

    AttachmentStatus next =
        switch (scanResult.verdict()) {
          case CLEAN -> AttachmentStatus.READY;
          case INFECTED, FAILED -> AttachmentStatus.FAILED;
        };

    if (next == AttachmentStatus.FAILED) {
      log.warn(
          "Attachment scan {}: {} ({})",
          scanResult.verdict(),
          scanResult.storageKey(),
          scanResult.reason());
    } else {
      log.info("Attachment scan passed: {}", scanResult.storageKey());
    }
    attachment.setStatus(next);
    attachmentRepository.save(attachment);
  }
}
