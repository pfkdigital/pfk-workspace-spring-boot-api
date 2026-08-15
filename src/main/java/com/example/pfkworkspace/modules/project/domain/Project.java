package com.example.pfkworkspace.modules.project.domain;

import com.example.pfkworkspace.common.persistence.BaseEntity;
import com.example.pfkworkspace.modules.task.domain.Task;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Entity
@Table(
    name = "projects",
    indexes = {
      @Index(name = "projects_workspace_id_idx", columnList = "workspace_id"),
      @Index(name = "projects_created_by_user_id_idx", columnList = "created_by_user_id")
    })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Project extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "workspace_id", nullable = false)
  private Workspace workspace;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "color")
  private String color;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ProjectStatus status;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "target_date")
  private LocalDate targetDate;

  @Column(name = "archived_at")
  private Instant archivedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_user_id")
  private User createdBy;

  @Builder.Default
  @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Task> tasks = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProjectLink> projectLinks = new ArrayList<>();

  public void addTask(Task task) {
    task.setProject(this);
    tasks.add(task);
  }

  public void removeTask(Task task) {
    tasks.remove(task);
    task.setProject(null);
  }

  public void addProjectLink(ProjectLink projectLink) {
    projectLink.setProject(this);
    projectLinks.add(projectLink);
  }

  public void removeProjectLink(ProjectLink projectLink) {
    projectLinks.remove(projectLink);
    projectLink.setProject(null);
  }
}
