package com.example.pfkworkspace.modules.user.infrastructure.repo;

import com.example.pfkworkspace.modules.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User,UUID> {}
