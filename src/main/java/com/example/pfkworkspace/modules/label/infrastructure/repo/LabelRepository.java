package com.example.pfkworkspace.modules.label.infrastructure.repo;

import com.example.pfkworkspace.modules.label.domain.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface LabelRepository extends JpaRepository<Label, UUID> {
    List<Label> findAllByIdInAndProjectId(Set<UUID> ids, UUID projectId);
}
