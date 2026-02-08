package com.example.pfkworkspace.modules.auth.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AuthenticateRequestDto(
    @NotBlank
        @Min(value = 4, message = "User name should be at least 4 characters long")
        @Max(value = 20, message = "User name should be at most 20 characters long")
        String username,
    @NotBlank
        @Min(value = 5, message = "Password should be at least 5 characters long")
        @Max(value = 10, message = "Password should be at most 20 characters long")
        String password) {}
