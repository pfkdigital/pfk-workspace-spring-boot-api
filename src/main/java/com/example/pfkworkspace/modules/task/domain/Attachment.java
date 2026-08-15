package com.example.pfkworkspace.modules.task.domain;

import com.example.pfkworkspace.common.persistence.BaseEntity;
import com.example.pfkworkspace.modules.project.domain.Project;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(
    name = "attachments",
    indexes = {
      @Index(name = "attachments_task_id_idx", columnList = "task_id"),
      @Index(name = "attachments_project_id_idx", columnList = "project_id"),
      @Index(name = "attachments_workspace_id_idx", columnList = "workspace_id"),
      @Index(name = "attachments_uploaded_by_user_id_idx", columnList = "uploaded_by_user_id")
    })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Attachment extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "workspace_id", nullable = false)
  private Workspace workspace;

  @Column(name = "filename", nullable = false)
  private String filename;

  @Column(name = "content_type", nullable = false)
  private String contentType;

  @Column(name = "extension", nullable = false)
  private String extension;

  @Column(name = "size_bytes", nullable = false)
  private long sizeBytes;

  @Column(name = "checksum", nullable = false)
  private String checksum;

  @Column(name = "storage_key", nullable = false, unique = true)
  private String storageKey;
  

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "uploaded_by_user_id", nullable = false)
  private User uploadedBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private AttachmentStatus status;
}
