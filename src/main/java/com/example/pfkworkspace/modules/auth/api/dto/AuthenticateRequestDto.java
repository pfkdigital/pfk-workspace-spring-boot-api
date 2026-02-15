package com.example.pfkworkspace.modules.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthenticateRequestDto(
    @NotBlank
        @Size(min = 4, max = 20, message = "User name should be between 4 and 20 characters long")
        String username,
    @NotBlank
        @Size(min = 5, max = 20, message = "Password should be between 5 and 20 characters long")
        String password) {}
