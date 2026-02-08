package com.example.pfkworkspace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;
import java.util.UUID;

@Configuration
public class AuditingConfig {

    @Bean
    public AuditorAware<UUID> auditorAware(CurrentUserIdProvider currentUserIdProvider) {
        return () -> Optional.ofNullable(currentUserIdProvider.getCurrentUserId());
    }

}
