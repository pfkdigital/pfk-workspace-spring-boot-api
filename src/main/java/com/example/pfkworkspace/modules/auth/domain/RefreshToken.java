package com.example.pfkworkspace.modules.auth.domain;

import com.example.pfkworkspace.common.persistence.BaseEntity;
import com.example.pfkworkspace.modules.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
      @Index(name = "idx_refresh_token_user_id", columnList = "user_id"),
      @Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at")
    },
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_refresh_tokens_token_hash", columnNames = "token_hash")
    })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken extends BaseEntity {

  @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
  private UUID user_id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  @Column(name = "token_hash", nullable = false, unique = true)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "revoked", nullable = false)
  private boolean revoked = false;
}
