package com.example.pfkworkspace.modules.workspace.infrastructure.repo;

import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {

  @Query(
      "SELECT m FROM WorkspaceMember m WHERE m.user.id = :userId AND m.workspace.id = :workspaceId")
  Optional<WorkspaceMember> findByUserIdAndWorkspaceId(
      @Param("userId") UUID userId, @Param("workspaceId") UUID workspaceId);

  @Query("SELECT m.workspace FROM WorkspaceMember m WHERE m.user.id = :userId")
  List<Workspace> findWorkspacesByUserId(@Param("userId") UUID userId);
}
