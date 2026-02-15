package com.example.pfkworkspace.modules.auth.domain;

import com.example.pfkworkspace.common.persistence.BaseEntity;
import com.example.pfkworkspace.modules.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "password_reset_tokens",
    indexes = {
      @Index(name = "idx_password_reset_tokens_user_id", columnList = "user_id"),
    })
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class PasswordResetToken extends BaseEntity {
  @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
  private UUID userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  @Column(name = "token_hash", nullable = false, unique = true)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "used", nullable = false)
  private boolean used = false;
}
