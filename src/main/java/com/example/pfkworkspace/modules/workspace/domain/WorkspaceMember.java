package com.example.pfkworkspace.modules.workspace.domain;

import com.example.pfkworkspace.common.persistence.BaseEntity;
import com.example.pfkworkspace.modules.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
    name = "workspace_members",
    indexes = {
      @Index(name = "workspace_members_user_id_idx", columnList = "user_id"),
      @Index(name = "workspace_members_workspace_id_idx", columnList = "workspace_id"),
      @Index(name = "workspace_members_role_idx", columnList = "role"),
    },
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_workspace_members_user_id_workspace_id",
          columnNames = {"user_id", "workspace_id"})
    })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceMember extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "workspace_id", nullable = false)
  private Workspace workspace;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private WorkspaceRole role;

  @Column(name = "joined_at", nullable = false)
  private Instant joinedAt;
}
