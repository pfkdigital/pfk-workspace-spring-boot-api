package com.example.pfkworkspace.modules.task.infrastructure.repo;

import com.example.pfkworkspace.modules.task.domain.Task;
import com.example.pfkworkspace.modules.task.domain.TaskStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

  Page<Task> findAllByProjectId(UUID projectId, Pageable pageable);

  Optional<Task> findByIdAndProjectId(UUID id, UUID projectId);

  @Query(
      "SELECT t.status AS status, COUNT(t) AS count FROM Task t WHERE t.project.id = :projectId GROUP BY t.status")
  List<TaskStatusCount> countByProjectIdGroupByStatus(@Param("projectId") UUID projectId);

  interface TaskStatusCount {
    TaskStatus getStatus();

    long getCount();
  }
}
