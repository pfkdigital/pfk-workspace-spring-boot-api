package com.example.pfkworkspace.modules.project.infrastructure.repo;

import com.example.pfkworkspace.modules.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByWorkspaceId(UUID workspaceId);
}
