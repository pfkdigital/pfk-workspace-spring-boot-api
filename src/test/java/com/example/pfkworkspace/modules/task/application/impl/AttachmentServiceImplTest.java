package com.example.pfkworkspace.modules.task.application.impl;

import com.example.pfkworkspace.common.aws.StorageException;
import com.example.pfkworkspace.common.aws.service.S3Service;
import com.example.pfkworkspace.common.error.ConflictException;
import com.example.pfkworkspace.modules.auth.application.UserContextService;
import com.example.pfkworkspace.modules.project.domain.Project;
import com.example.pfkworkspace.modules.project.domain.ProjectStatus;
import com.example.pfkworkspace.modules.task.api.dto.request.CreateAttachmentRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.CreateAttachmentResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.GetAttachmentResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.RemoveAttachmentResponseDto;
import com.example.pfkworkspace.modules.task.api.exception.AttachmentNotFoundException;
import com.example.pfkworkspace.modules.task.application.TaskAccessService;
import com.example.pfkworkspace.modules.task.application.messaging.ScanResultMessage;
import com.example.pfkworkspace.modules.task.application.messaging.ScanVerdict;
import com.example.pfkworkspace.modules.task.domain.Attachment;
import com.example.pfkworkspace.modules.task.domain.AttachmentStatus;
import com.example.pfkworkspace.modules.task.domain.Task;
import com.example.pfkworkspace.modules.task.domain.TaskPriority;
import com.example.pfkworkspace.modules.task.domain.TaskStatus;
import com.example.pfkworkspace.modules.task.infrastructure.repo.AttachmentRepository;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Mocking notes
 * - the storage key is generated inside the service, so an ArgumentCaptor on save() is the only
 *   way to assert its shape
 * - stubbing is per test rather than in a shared @BeforeEach: re-stubbing a method that a shared
 *   setup already stubbed trips Mockito's strict-stubs check
 * - @PreAuthorize is not exercised here - a Mockito test has no Spring proxy. See
 *   WorkspaceServiceImplSecurityTest for the context based pattern.
 */
@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    private static final Duration URL_EXPIRY = Duration.ofMinutes(15);
    private static final String PRESIGNED_URL = "https://s3.example.com/presigned";

    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private TaskAccessService taskAccessService;
    @Mock
    private UserContextService userContextService;
    @Mock
    private S3Service s3Service;

    private AttachmentServiceImpl attachmentService;

    private UUID workspaceId;
    private UUID projectId;
    private UUID taskId;
    private UUID attachmentId;
    private Task task;
    private Project project;
    private Workspace workspace;
    private User currentUser;

    @BeforeEach
    void setUp() {
        attachmentService = new AttachmentServiceImpl(
                attachmentRepository, taskAccessService, userContextService, s3Service);

        workspaceId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        attachmentId = UUID.randomUUID();

        currentUser = User.builder().email("user@example.com").username("user").build();
        currentUser.setId(UUID.randomUUID());

        workspace = Workspace.builder().name("Workspace").owner(currentUser).build();
        workspace.setId(workspaceId);

        project = Project.builder().name("Project").status(ProjectStatus.ACTIVE).workspace(workspace).build();
        project.setId(projectId);

        task = Task.builder()
                .project(project)
                .workspace(workspace)
                .title("Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();
        task.setId(taskId);
    }

    private CreateAttachmentRequestDto request(String filename, String extension, String checksum) {
        return new CreateAttachmentRequestDto(filename, extension, "application/pdf", 1024L, checksum);
    }

    private CreateAttachmentRequestDto request() {
        return request("report.pdf", "pdf", "a".repeat(64));
    }

    private Attachment attachment(AttachmentStatus status) {
        Attachment attachment = Attachment.builder()
                .task(task)
                .project(project)
                .workspace(workspace)
                .filename("report.pdf")
                .contentType("application/pdf")
                .extension("pdf")
                .sizeBytes(1024L)
                .checksum("a".repeat(64))
                .storageKey("workspaces/w/projects/p/tasks/t/attachments/" + UUID.randomUUID() + ".pdf")
                .uploadedBy(currentUser)
                .status(status)
                .build();
        attachment.setId(attachmentId);
        return attachment;
    }

    private Attachment captureSaved() {
        ArgumentCaptor<Attachment> captor = ArgumentCaptor.forClass(Attachment.class);
        verify(attachmentRepository).save(captor.capture());
        return captor.getValue();
    }

    @Nested
    @DisplayName("createAttachment")
    class CreateAttachment {

        private void givenTaskAndUserResolve() {
            when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
            when(userContextService.getCurrentUser()).thenReturn(currentUser);
        }

        private void givenPresignSucceeds() {
            when(s3Service.generateUploadUrl(anyString(), anyString(), anyLong(), anyString(), eq(URL_EXPIRY)))
                    .thenReturn(PRESIGNED_URL);
        }

        @Test
        void shouldPersistAttachmentAsPendingWithRequestMetadata() {
            givenTaskAndUserResolve();
            givenPresignSucceeds();

            attachmentService.createAttachment(workspaceId, projectId, taskId, request());

            Attachment saved = captureSaved();
            assertThat(saved.getStatus()).isEqualTo(AttachmentStatus.PENDING);
            assertThat(saved.getFilename()).isEqualTo("report.pdf");
            assertThat(saved.getContentType()).isEqualTo("application/pdf");
            assertThat(saved.getSizeBytes()).isEqualTo(1024L);
            assertThat(saved.getUploadedBy()).isEqualTo(currentUser);
        }

        @Test
        void shouldLowercaseExtensionAndChecksum() {
            givenTaskAndUserResolve();
            givenPresignSucceeds();

            attachmentService.createAttachment(
                    workspaceId, projectId, taskId, request("report.PDF", "PDF", "A".repeat(64)));

            Attachment saved = captureSaved();
            assertThat(saved.getExtension()).isEqualTo("pdf");
            assertThat(saved.getChecksum()).isEqualTo("a".repeat(64));
        }

        @Test
        void shouldBuildTenantScopedStorageKey() {
            givenTaskAndUserResolve();
            givenPresignSucceeds();

            attachmentService.createAttachment(workspaceId, projectId, taskId, request());

            assertThat(captureSaved().getStorageKey())
                    .matches("workspaces/%s/projects/%s/tasks/%s/attachments/[0-9a-f-]{36}\\.pdf"
                            .formatted(workspaceId, projectId, taskId));
        }

        @Test
        void shouldGenerateADifferentStorageKeyOnEveryCall() {
            givenTaskAndUserResolve();
            givenPresignSucceeds();

            attachmentService.createAttachment(workspaceId, projectId, taskId, request());
            attachmentService.createAttachment(workspaceId, projectId, taskId, request());

            ArgumentCaptor<Attachment> captor = ArgumentCaptor.forClass(Attachment.class);
            verify(attachmentRepository, times(2)).save(captor.capture());
            List<Attachment> saved = captor.getAllValues();
            assertThat(saved.get(0).getStorageKey()).isNotEqualTo(saved.get(1).getStorageKey());
        }

        @Test
        void shouldTakeProjectAndWorkspaceFromTheResolvedTask() {
            givenTaskAndUserResolve();
            givenPresignSucceeds();

            attachmentService.createAttachment(workspaceId, projectId, taskId, request());

            Attachment saved = captureSaved();
            assertThat(saved.getProject()).isSameAs(project);
            assertThat(saved.getWorkspace()).isSameAs(workspace);
        }

        @Test
        void shouldLinkTheAttachmentToTheTask() {
            givenTaskAndUserResolve();
            givenPresignSucceeds();

            attachmentService.createAttachment(workspaceId, projectId, taskId, request());

            Attachment saved = captureSaved();
            assertThat(task.getAttachments()).containsExactly(saved);
            assertThat(saved.getTask()).isSameAs(task);
        }

        @Test
        void shouldBindContentTypeSizeAndChecksumIntoThePresignedUrl() {
            givenTaskAndUserResolve();
            givenPresignSucceeds();

            attachmentService.createAttachment(workspaceId, projectId, taskId, request());

            String storageKey = captureSaved().getStorageKey();
            verify(s3Service).generateUploadUrl(
                    storageKey, "application/pdf", 1024L, "a".repeat(64), URL_EXPIRY);
        }

        @Test
        void shouldReturnFilenameUrlAndStatus() {
            givenTaskAndUserResolve();
            givenPresignSucceeds();

            CreateAttachmentResponseDto response =
                    attachmentService.createAttachment(workspaceId, projectId, taskId, request());

            assertThat(response.getFilename()).isEqualTo("report.pdf");
            assertThat(response.getPreSignedUrl()).isEqualTo(PRESIGNED_URL);
            assertThat(response.getStatus()).isEqualTo(AttachmentStatus.PENDING);
        }

        @Test
        void shouldPropagateWhenTaskIsNotInThatWorkspaceProject() {
            when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId))
                    .thenThrow(new IllegalStateException("not in project"));

            assertThatThrownBy(() ->
                    attachmentService.createAttachment(workspaceId, projectId, taskId, request()))
                    .isInstanceOf(IllegalStateException.class);

            verifyNoInteractions(attachmentRepository, s3Service);
        }

        @Test
        void shouldPropagateStorageExceptionWhenPresigningFails() {
            givenTaskAndUserResolve();
            when(s3Service.generateUploadUrl(anyString(), anyString(), anyLong(), anyString(), eq(URL_EXPIRY)))
                    .thenThrow(new StorageException("boom", new RuntimeException()));

            assertThatThrownBy(() ->
                    attachmentService.createAttachment(workspaceId, projectId, taskId, request()))
                    .isInstanceOf(StorageException.class);
        }
    }

    @Nested
    @DisplayName("getAttachment")
    class GetAttachment {

        @Test
        void shouldReturnAPresignedDownloadUrlWhenReady() {
            Attachment ready = attachment(AttachmentStatus.READY);
            when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
            when(attachmentRepository.findByIdAndTaskId(attachmentId, taskId)).thenReturn(Optional.of(ready));
            when(s3Service.generateDownloadUrl(anyString(), anyString(), anyString(), eq(URL_EXPIRY)))
                    .thenReturn(PRESIGNED_URL);

            GetAttachmentResponseDto response =
                    attachmentService.getAttachment(workspaceId, projectId, taskId, attachmentId);

            assertThat(response.getPresignedUrl()).isEqualTo(PRESIGNED_URL);
            assertThat(response.getAttachmentId()).isEqualTo(attachmentId);
            assertThat(response.getFileName()).isEqualTo("report.pdf");
            assertThat(response.getContentType()).isEqualTo("application/pdf");
            assertThat(response.getAttachmentSize()).isEqualTo(1024L);
        }

        @Test
        void shouldPassFilenameAndContentTypeToThePresigner() {
            Attachment ready = attachment(AttachmentStatus.READY);
            when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
            when(attachmentRepository.findByIdAndTaskId(attachmentId, taskId)).thenReturn(Optional.of(ready));
            when(s3Service.generateDownloadUrl(anyString(), anyString(), anyString(), eq(URL_EXPIRY)))
                    .thenReturn(PRESIGNED_URL);

            attachmentService.getAttachment(workspaceId, projectId, taskId, attachmentId);

            verify(s3Service).generateDownloadUrl(
                    ready.getStorageKey(), "application/pdf", "report.pdf", URL_EXPIRY);
        }

        @Test
        void shouldThrowNotFoundWhenNoAttachmentMatchesThatIdAndTask() {
            when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
            when(attachmentRepository.findByIdAndTaskId(attachmentId, taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    attachmentService.getAttachment(workspaceId, projectId, taskId, attachmentId))
                    .isInstanceOf(AttachmentNotFoundException.class);
        }

        @ParameterizedTest
        @EnumSource(value = AttachmentStatus.class, names = {"PENDING", "PROCESSING", "FAILED"})
        void shouldThrowConflictWhenTheAttachmentIsNotReady(AttachmentStatus status) {
            when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
            when(attachmentRepository.findByIdAndTaskId(attachmentId, taskId))
                    .thenReturn(Optional.of(attachment(status)));

            assertThatThrownBy(() ->
                    attachmentService.getAttachment(workspaceId, projectId, taskId, attachmentId))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        void shouldNotPresignAnythingWhenTheStatusCheckFails() {
            when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
            when(attachmentRepository.findByIdAndTaskId(attachmentId, taskId))
                    .thenReturn(Optional.of(attachment(AttachmentStatus.PENDING)));

            assertThatThrownBy(() ->
                    attachmentService.getAttachment(workspaceId, projectId, taskId, attachmentId))
                    .isInstanceOf(ConflictException.class);

            verifyNoInteractions(s3Service);
        }

        @Test
        void shouldPropagateWhenTaskIsNotInThatWorkspaceProject() {
            when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId))
                    .thenThrow(new IllegalStateException("not in project"));

            assertThatThrownBy(() ->
                    attachmentService.getAttachment(workspaceId, projectId, taskId, attachmentId))
                    .isInstanceOf(IllegalStateException.class);

            verifyNoInteractions(attachmentRepository, s3Service);
        }
    }

    @Nested
    @DisplayName("deleteAttachment")
    class DeleteAttachment {

        private Attachment givenAttachmentOnTask(AttachmentStatus status) {
            Attachment attachment = attachment(status);
            task.addAttachment(attachment);
            when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
            when(attachmentRepository.findByIdAndTaskId(attachmentId, taskId)).thenReturn(Optional.of(attachment));
            return attachment;
        }

        @Test
        void shouldUnlinkFromTheTaskAndDeleteTheRow() {
            Attachment attachment = givenAttachmentOnTask(AttachmentStatus.READY);

            attachmentService.deleteAttachment(workspaceId, projectId, taskId, attachmentId);

            assertThat(task.getAttachments()).isEmpty();
            verify(attachmentRepository).delete(attachment);
        }

        @Test
        void shouldDeleteFromTheCleanBucketWhenReady() {
            Attachment attachment = givenAttachmentOnTask(AttachmentStatus.READY);

            attachmentService.deleteAttachment(workspaceId, projectId, taskId, attachmentId);

            verify(s3Service).deleteObject(attachment.getStorageKey());
            verify(s3Service, never()).deleteQuarantinedObject(anyString());
        }

        @Test
        void shouldDeleteFromQuarantineWhenPending() {
            Attachment attachment = givenAttachmentOnTask(AttachmentStatus.PENDING);

            attachmentService.deleteAttachment(workspaceId, projectId, taskId, attachmentId);

            verify(s3Service).deleteQuarantinedObject(attachment.getStorageKey());
            verify(s3Service, never()).deleteObject(anyString());
        }

        @ParameterizedTest
        @EnumSource(value = AttachmentStatus.class, names = {"PROCESSING", "FAILED"})
        void shouldDeleteFromQuarantineWhenNotYetPromoted(AttachmentStatus status) {
            Attachment attachment = givenAttachmentOnTask(status);

            attachmentService.deleteAttachment(workspaceId, projectId, taskId, attachmentId);

            verify(s3Service).deleteQuarantinedObject(attachment.getStorageKey());
            verify(s3Service, never()).deleteObject(anyString());
        }

        @Test
        void shouldThrowNotFoundWhenNoAttachmentMatchesThatIdAndTask() {
            when(taskAccessService.getTaskInWorkspaceProject(workspaceId, projectId, taskId)).thenReturn(task);
            when(attachmentRepository.findByIdAndTaskId(attachmentId, taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    attachmentService.deleteAttachment(workspaceId, projectId, taskId, attachmentId))
                    .isInstanceOf(AttachmentNotFoundException.class);

            verifyNoInteractions(s3Service);
        }

        @Test
        void shouldReturnTheAttachmentIdAndTaskId() {
            givenAttachmentOnTask(AttachmentStatus.READY);

            RemoveAttachmentResponseDto response =
                    attachmentService.deleteAttachment(workspaceId, projectId, taskId, attachmentId);

            assertThat(response.getAttachmentId()).isEqualTo(attachmentId);
            assertThat(response.getTaskId()).isEqualTo(taskId);
        }
    }

    @Nested
    @DisplayName("applyScanResult")
    class ApplyScanResult {

        private ScanResultMessage scanResult(String storageKey, ScanVerdict verdict) {
            return new ScanResultMessage(storageKey, "pfk-workspace", verdict, "reason");
        }

        @Test
        void cleanVerdictShouldSetTheStatusToReady() {
            Attachment attachment = attachment(AttachmentStatus.PENDING);
            when(attachmentRepository.findByStorageKey(attachment.getStorageKey()))
                    .thenReturn(Optional.of(attachment));

            attachmentService.applyScanResult(scanResult(attachment.getStorageKey(), ScanVerdict.CLEAN));

            assertThat(attachment.getStatus()).isEqualTo(AttachmentStatus.READY);
            verify(attachmentRepository).save(attachment);
        }

        @ParameterizedTest
        @EnumSource(value = ScanVerdict.class, names = {"INFECTED", "FAILED"})
        void unsuccessfulVerdictShouldSetTheStatusToFailed(ScanVerdict verdict) {
            Attachment attachment = attachment(AttachmentStatus.PENDING);
            when(attachmentRepository.findByStorageKey(attachment.getStorageKey()))
                    .thenReturn(Optional.of(attachment));

            attachmentService.applyScanResult(scanResult(attachment.getStorageKey(), verdict));

            assertThat(attachment.getStatus()).isEqualTo(AttachmentStatus.FAILED);
            verify(attachmentRepository).save(attachment);
        }

        @Test
        void shouldApplyAVerdictToAnAttachmentThatIsProcessing() {
            Attachment attachment = attachment(AttachmentStatus.PROCESSING);
            when(attachmentRepository.findByStorageKey(attachment.getStorageKey()))
                    .thenReturn(Optional.of(attachment));

            attachmentService.applyScanResult(scanResult(attachment.getStorageKey(), ScanVerdict.CLEAN));

            assertThat(attachment.getStatus()).isEqualTo(AttachmentStatus.READY);
        }

        @ParameterizedTest
        @EnumSource(value = AttachmentStatus.class, names = {"READY", "FAILED"})
        void shouldIgnoreRedeliveryWhenTheAttachmentIsAlreadyResolved(AttachmentStatus status) {
            Attachment attachment = attachment(status);
            when(attachmentRepository.findByStorageKey(attachment.getStorageKey()))
                    .thenReturn(Optional.of(attachment));

            attachmentService.applyScanResult(scanResult(attachment.getStorageKey(), ScanVerdict.CLEAN));

            assertThat(attachment.getStatus()).isEqualTo(status);
            verify(attachmentRepository, never()).save(any());
            verifyNoInteractions(s3Service);
        }

        @Test
        void whenTheRowIsGoneACleanVerdictShouldDeleteTheOrphanedObject() {
            String storageKey = "workspaces/w/projects/p/tasks/t/attachments/orphan.pdf";
            when(attachmentRepository.findByStorageKey(storageKey)).thenReturn(Optional.empty());

            attachmentService.applyScanResult(scanResult(storageKey, ScanVerdict.CLEAN));

            verify(s3Service).deleteObject(storageKey);
        }

        @ParameterizedTest
        @EnumSource(value = ScanVerdict.class, names = {"INFECTED", "FAILED"})
        void whenTheRowIsGoneAnUnsuccessfulVerdictShouldNotTouchS3(ScanVerdict verdict) {
            String storageKey = "workspaces/w/projects/p/tasks/t/attachments/orphan.pdf";
            when(attachmentRepository.findByStorageKey(storageKey)).thenReturn(Optional.empty());

            attachmentService.applyScanResult(scanResult(storageKey, verdict));

            verifyNoInteractions(s3Service);
        }
    }
}
