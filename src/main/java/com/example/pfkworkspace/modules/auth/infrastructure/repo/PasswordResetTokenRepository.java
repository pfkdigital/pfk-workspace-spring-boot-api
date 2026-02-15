package com.example.pfkworkspace.modules.auth.infrastructure.repo;

import com.example.pfkworkspace.modules.auth.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findPasswordResetTokenByUserId(UUID userId);

    Optional<PasswordResetToken> findPasswordResetTokenByTokenHash(String tokenHash);
}
