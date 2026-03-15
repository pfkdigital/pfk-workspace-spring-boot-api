package com.example.pfkworkspace.modules.workspace.domain;

import com.example.pfkworkspace.common.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
    name = "workspace_invitations",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_workspace_invitations_token_hash", columnNames = "token_hash")
    },
    indexes = {
      @Index(name = "workspace_invitations_workspace_id_idx", columnList = "workspace_id"),
      @Index(name = "workspace_invitations_email_idx", columnList = "email"),
      @Index(name = "workspace_invitations_token_hash_idx", columnList = "token_hash")
    })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceInvitation extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "workspace_id", nullable = false)
  private Workspace workspace;

  @Column(name = "email", nullable = false)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private WorkspaceRole role;

  @Column(name = "token_hash", nullable = false)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "is_used", nullable = false)
  private Boolean isUsed = false;
}
