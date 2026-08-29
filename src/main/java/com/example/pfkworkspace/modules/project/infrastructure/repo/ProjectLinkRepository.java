package com.example.pfkworkspace.modules.project.infrastructure.repo;

import com.example.pfkworkspace.modules.project.domain.ProjectLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectLinkRepository extends JpaRepository<ProjectLink, UUID> {
    boolean existsByLabel(String label);

    boolean existsByUrl(String url);
}
