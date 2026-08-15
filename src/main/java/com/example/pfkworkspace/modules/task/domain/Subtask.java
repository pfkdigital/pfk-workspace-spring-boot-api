package com.example.pfkworkspace.modules.task.domain;

import com.example.pfkworkspace.common.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "subtasks",
    indexes = {@Index(name = "subtasks_task_id_idx", columnList = "task_id")})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Subtask extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "done", nullable = false)
  private boolean done;
}
