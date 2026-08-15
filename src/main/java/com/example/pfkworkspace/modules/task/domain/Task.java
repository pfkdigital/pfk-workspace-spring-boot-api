package com.example.pfkworkspace.modules.task.domain;

import com.example.pfkworkspace.common.persistence.BaseEntity;
import com.example.pfkworkspace.modules.label.domain.Label;
import com.example.pfkworkspace.modules.project.domain.Project;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.*;

@Entity
@Table(
    name = "tasks",
    indexes = {
      @Index(name = "tasks_project_id_idx", columnList = "project_id"),
      @Index(name = "tasks_workspace_id_idx", columnList = "workspace_id"),
      @Index(name = "tasks_assignee_user_id_idx", columnList = "assignee_user_id"),
      @Index(name = "tasks_status_idx", columnList = "status")
    })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Task extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "workspace_id", nullable = false)
  private Workspace workspace;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private TaskStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "priority", nullable = false)
  private TaskPriority priority;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assignee_user_id")
  private User assignee;

  @Column(name = "due_date")
  private LocalDate dueDate;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Builder.Default
  @ManyToMany
  @JoinTable(
      name = "task_labels",
      joinColumns = @JoinColumn(name = "task_id"),
      inverseJoinColumns = @JoinColumn(name = "label_id"))
  private Set<Label> labels = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Subtask> subtasks = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Attachment> attachments = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> comments = new ArrayList<>();

  public void addLabel(Label label) {
    labels.add(label);
  }

  public void removeLabel(Label label) {
    labels.remove(label);
  }

  public void addSubtask(Subtask subtask) {
    subtask.setTask(this);
    subtasks.add(subtask);
  }

  public void removeSubtask(Subtask subtask) {
    subtasks.remove(subtask);
    subtask.setTask(null);
  }

  public void addAttachment(Attachment attachment) {
    attachment.setTask(this);
    attachments.add(attachment);
  }

  public void removeAttachment(Attachment attachment) {
    attachments.remove(attachment);
    attachment.setTask(null);
  }

  public void addComment(Comment comment) {
    comment.setTask(this);
    comments.add(comment);
  }

  public void removeComment(Comment comment) {
    comments.remove(comment);
    comment.setTask(null);
  }
}
