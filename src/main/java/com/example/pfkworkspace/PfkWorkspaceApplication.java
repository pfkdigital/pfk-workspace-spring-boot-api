package com.example.pfkworkspace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PfkWorkspaceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PfkWorkspaceApplication.class, args);
    }
}
