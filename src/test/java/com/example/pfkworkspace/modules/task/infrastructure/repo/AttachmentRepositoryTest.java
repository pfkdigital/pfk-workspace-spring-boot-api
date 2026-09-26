package com.example.pfkworkspace.modules.task.infrastructure.repo;

import com.example.pfkworkspace.TestcontainersConfiguration;
import com.example.pfkworkspace.config.PersistenceConfig;
import com.example.pfkworkspace.modules.project.domain.Project;
import com.example.pfkworkspace.modules.project.domain.ProjectStatus;
import com.example.pfkworkspace.modules.project.infrastructure.repo.ProjectRepository;
import com.example.pfkworkspace.modules.task.domain.Attachment;
import com.example.pfkworkspace.modules.task.domain.AttachmentStatus;
import com.example.pfkworkspace.modules.task.domain.Task;
import com.example.pfkworkspace.modules.task.domain.TaskPriority;
import com.example.pfkworkspace.modules.task.domain.TaskStatus;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.user.infrastructure.repo.UserRepository;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, PersistenceConfig.class})
class AttachmentRepositoryTest {

    @Autowired
    private AttachmentRepository attachmentRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private WorkspaceRepository workspaceRepository;
    @Autowired
    private UserRepository userRepository;

    private Task taskA;
    private Task taskB;
    private User uploader;
    private Attachment pendingOnTaskA;
    private Attachment pendingOnTaskB;
    private Attachment readyOnTaskA;

    @BeforeEach
    void setUp() {
        User owner = userRepository.save(
                User.builder()
                        .email("owner-" + UUID.randomUUID() + "@example.com")
                        .username("owner-" + UUID.randomUUID())
                        .firstName("Owner")
                        .lastName("User")
                        .passwordHash("hashed")
                        .build());

        uploader = userRepository.save(
                User.builder()
                        .email("uploader-" + UUID.randomUUID() + "@example.com")
                        .username("uploader-" + UUID.randomUUID())
                        .firstName("Uploader")
                        .lastName("User")
                        .passwordHash("hashed")
                        .build());

        Workspace workspace = workspaceRepository.save(Workspace.builder().name("Workspace").owner(owner).build());
        Project project = projectRepository.save(
                Project.builder().name("Project").status(ProjectStatus.ACTIVE).workspace(workspace).build());

        taskA = taskRepository.save(
                Task.builder().project(project).workspace(workspace).title("Task A")
                        .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).build());
        taskB = taskRepository.save(
                Task.builder().project(project).workspace(workspace).title("Task B")
                        .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).build());

        pendingOnTaskA = attachmentRepository.save(newAttachment(taskA, AttachmentStatus.PENDING));
        pendingOnTaskB = attachmentRepository.save(newAttachment(taskB, AttachmentStatus.PENDING));
        readyOnTaskA = attachmentRepository.save(newAttachment(taskA, AttachmentStatus.READY));
    }

    @Test
    void findByStorageKey_WhenStorageKeyExists_ShouldReturnAttachment() {
        Optional<Attachment> result = attachmentRepository.findByStorageKey(pendingOnTaskA.getStorageKey());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(pendingOnTaskA.getId());
    }

    @Test
    void findByStorageKey_WhenStorageKeyDoesNotExist_ShouldReturnEmpty() {
        Optional<Attachment> result =
                attachmentRepository.findByStorageKey("workspaces/none/attachments/missing.pdf");

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndTaskId_WhenAttachmentBelongsToTask_ShouldReturnAttachment() {
        Optional<Attachment> result =
                attachmentRepository.findByIdAndTaskId(pendingOnTaskA.getId(), taskA.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(pendingOnTaskA.getId());
    }

    @Test
    void findByIdAndTaskId_WhenAttachmentDoesNotExist_ShouldReturnEmpty() {
        Optional<Attachment> result =
                attachmentRepository.findByIdAndTaskId(UUID.randomUUID(), taskA.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndTaskId_WhenAttachmentBelongsToDifferentTask_ShouldReturnEmpty() {
        Optional<Attachment> result =
                attachmentRepository.findByIdAndTaskId(pendingOnTaskA.getId(), taskB.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByStatusAndCreatedAtBefore_WhenRowsAreOlderThanCutOff_ShouldReturnThem() {
        List<Attachment> result = attachmentRepository.findAllByStatusAndCreatedAtBefore(
                AttachmentStatus.PENDING, Instant.now().plus(1, ChronoUnit.MINUTES));

        assertThat(result)
                .extracting(Attachment::getId)
                .containsExactlyInAnyOrder(pendingOnTaskA.getId(), pendingOnTaskB.getId());
    }

    @Test
    void findAllByStatusAndCreatedAtBefore_ShouldExcludeOtherStatuses() {
        List<Attachment> result = attachmentRepository.findAllByStatusAndCreatedAtBefore(
                AttachmentStatus.PENDING, Instant.now().plus(1, ChronoUnit.MINUTES));

        assertThat(result).extracting(Attachment::getId).doesNotContain(readyOnTaskA.getId());
    }

    @Test
    void findAllByStatusAndCreatedAtBefore_WhenNoRowsHaveThatStatus_ShouldReturnEmptyList() {
        List<Attachment> result = attachmentRepository.findAllByStatusAndCreatedAtBefore(
                AttachmentStatus.FAILED, Instant.now().plus(1, ChronoUnit.MINUTES));

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByStatusAndCreatedAtBefore_WhenCutOffPredatesCreation_ShouldReturnEmptyList() {
        List<Attachment> result = attachmentRepository.findAllByStatusAndCreatedAtBefore(
                AttachmentStatus.PENDING, Instant.now().minus(1, ChronoUnit.MINUTES));

        assertThat(result).isEmpty();
    }

    @Test
    void save_ShouldPersistAssociationsAndMetadata() {
        Optional<Attachment> result = attachmentRepository.findById(pendingOnTaskA.getId());

        assertThat(result).isPresent();
        Attachment saved = result.get();
        assertThat(saved.getTask().getId()).isEqualTo(taskA.getId());
        assertThat(saved.getProject().getId()).isEqualTo(taskA.getProject().getId());
        assertThat(saved.getWorkspace().getId()).isEqualTo(taskA.getWorkspace().getId());
        assertThat(saved.getUploadedBy().getId()).isEqualTo(uploader.getId());
        assertThat(saved.getFilename()).isEqualTo("report.pdf");
        assertThat(saved.getContentType()).isEqualTo("application/pdf");
        assertThat(saved.getExtension()).isEqualTo("pdf");
        assertThat(saved.getSizeBytes()).isEqualTo(1024L);
        assertThat(saved.getChecksum()).hasSize(64);
        assertThat(saved.getStatus()).isEqualTo(AttachmentStatus.PENDING);
    }

    @Test
    void save_ShouldPopulateAuditFields() {
        Optional<Attachment> result = attachmentRepository.findById(pendingOnTaskA.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getCreatedAt()).isNotNull();
        assertThat(result.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void save_WhenStorageKeyAlreadyUsed_ShouldViolateUniqueConstraint() {
        Attachment duplicate = newAttachment(taskA, AttachmentStatus.PENDING);
        duplicate.setStorageKey(pendingOnTaskA.getStorageKey());

        assertThatThrownBy(() -> attachmentRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void delete_ShouldRemoveAttachment() {
        attachmentRepository.delete(pendingOnTaskA);

        assertThat(attachmentRepository.findById(pendingOnTaskA.getId())).isEmpty();
    }

    private Attachment newAttachment(Task task, AttachmentStatus status) {
        return Attachment.builder()
                .task(task)
                .project(task.getProject())
                .workspace(task.getWorkspace())
                .filename("report.pdf")
                .contentType("application/pdf")
                .extension("pdf")
                .sizeBytes(1024L)
                .checksum("a".repeat(64))
                .storageKey("workspaces/" + task.getWorkspace().getId()
                        + "/tasks/" + task.getId()
                        + "/attachments/" + UUID.randomUUID() + ".pdf")
                .uploadedBy(uploader)
                .status(status)
                .build();
    }
}
