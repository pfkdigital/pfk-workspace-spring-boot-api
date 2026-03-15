package com.example.pfkworkspace.modules.workspace.infrastructure.repo;

import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {}
