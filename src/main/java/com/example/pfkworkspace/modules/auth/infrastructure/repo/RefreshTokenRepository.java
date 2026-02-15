package com.example.pfkworkspace.modules.auth.infrastructure.repo;

import com.example.pfkworkspace.modules.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
        Optional<RefreshToken> findByTokenHash(String tokenHash);

        @Modifying
        @Query("delete from RefreshToken rt where rt.user_id = :userId")
        void deleteAllByUserId(@Param("userId") UUID userId);
}
