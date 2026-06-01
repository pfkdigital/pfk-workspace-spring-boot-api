package com.example.pfkworkspace.modules.workspace.infrastructure.repo;

import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
  @Query(
      "SELECT w from Workspace w LEFT JOIN FETCH w.owner LEFT JOIN FETCH w.workspaceMembers m LEFT JOIN FETCH m.user WHERE w.id = :id")
  Optional<Workspace> findByIdWithDetails(@Param("id") UUID workspaceId);
}
