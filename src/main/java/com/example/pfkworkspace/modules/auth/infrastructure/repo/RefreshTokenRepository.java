package com.example.pfkworkspace.modules.auth.infrastructure.repo;

import com.example.pfkworkspace.modules.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
        Optional<RefreshToken> findByTokenHash(String tokenHash);
}
