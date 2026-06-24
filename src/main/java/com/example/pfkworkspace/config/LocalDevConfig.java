package com.example.pfkworkspace.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
@Slf4j
public class LocalDevConfig {

  @Bean
  public FlywayMigrationStrategy cleanAndMigrate() {
    return flyway -> {
      log.info("Local profile: wiping and remigrating database");
      flyway.clean();
      flyway.migrate();
    };
  }
}
