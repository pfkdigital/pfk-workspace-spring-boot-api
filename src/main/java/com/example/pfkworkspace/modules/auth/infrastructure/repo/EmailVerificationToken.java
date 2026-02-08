package com.example.pfkworkspace.modules.auth.infrastructure.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmailVerificationToken extends JpaRepository<EmailVerificationToken, UUID> {}
