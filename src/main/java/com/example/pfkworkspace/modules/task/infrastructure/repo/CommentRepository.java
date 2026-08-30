package com.example.pfkworkspace.modules.task.infrastructure.repo;

import com.example.pfkworkspace.modules.task.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {}
