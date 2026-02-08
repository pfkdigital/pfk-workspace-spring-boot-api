package com.example.pfkworkspace;

import org.springframework.boot.SpringApplication;

public class TestPfkWorkspaceApplication {

    public static void main(String[] args) {
        SpringApplication.from(PfkWorkspaceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
