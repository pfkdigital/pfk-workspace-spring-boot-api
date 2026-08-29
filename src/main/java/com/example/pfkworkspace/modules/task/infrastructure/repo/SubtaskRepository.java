package com.example.pfkworkspace.modules.task.infrastructure.repo;

import com.example.pfkworkspace.modules.task.domain.Subtask;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubtaskRepository extends JpaRepository<Subtask, UUID> {
  Optional<Subtask> findByIdAndTaskId(UUID id, UUID taskId);
}
