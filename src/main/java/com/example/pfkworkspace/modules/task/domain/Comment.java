package com.example.pfkworkspace.modules.task.domain;

import com.example.pfkworkspace.common.persistence.BaseEntity;
import com.example.pfkworkspace.modules.user.domain.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(
    name = "comments",
    indexes = {
      @Index(name = "comments_task_id_idx", columnList = "task_id"),
      @Index(name = "comments_author_user_id_idx", columnList = "author_user_id")
    })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comment extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_user_id", nullable = false)
  private User author;

  @Column(name = "body", nullable = false, columnDefinition = "TEXT")
  private String body;

  @Column(name = "edited_at")
  private Instant editedAt;
}
