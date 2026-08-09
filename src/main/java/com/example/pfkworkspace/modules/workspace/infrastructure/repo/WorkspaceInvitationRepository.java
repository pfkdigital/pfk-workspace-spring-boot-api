package com.example.pfkworkspace.modules.workspace.infrastructure.repo;

import com.example.pfkworkspace.modules.workspace.domain.WorkspaceInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, UUID> {
    Optional<WorkspaceInvitation> findByTokenHash(String tokenHash);
    boolean existsByEmailAndWorkspace_IdAndIsUsedFalseAndExpiresAtAfter(String email, UUID workspaceId, Instant now);
}
