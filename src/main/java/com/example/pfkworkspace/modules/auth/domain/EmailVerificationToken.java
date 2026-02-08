package com.example.pfkworkspace.modules.auth.domain;

import com.example.pfkworkspace.common.persistence.BaseEntity;
import com.example.pfkworkspace.modules.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "email_verification_tokens",
    indexes = {
      @Index(name = "idx_email_verification_token_user_id", columnList = "user_id"),
            @Index(name = "idx_email_verification_token_token_hash", columnList = "token_hash"),
      @Index(name = "idx_email_verification_token_expires_at", columnList = "expires_at")
    },
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_email_verification_token_token_hash", columnNames = "token_hash")
    })
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class EmailVerificationToken extends BaseEntity {
  @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
  private UUID userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  @Column(name = "token_hash", nullable = false, unique = true)
  private String tokenHash;

  @Column(name = "used", nullable = false)
  private boolean used = false;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;
}
