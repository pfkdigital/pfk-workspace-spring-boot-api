package com.example.pfkworkspace.modules.auth.api.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDto(
        @NotBlank
        @Email(message = "Email should be valid")
        String email,

        @NotBlank
        @Min(value = 5, message = "Password should be at least 5 characters long")
        @Max(value = 10, message = "Password should be at most 20 characters long")
        String password,

        @NotBlank
        @Min(value = 4, message = "First name should be at least 4 characters long")
        @Max(value = 20, message = "First name should be at most 20 characters long")
        String firstName,

        @NotBlank
        @Min(value = 4, message = "Last name should be at least 4 characters long")
        @Max(value = 20, message = "Last name should be at most 20 characters long")
        String lastName,

        @NotBlank
        @Min(value = 4, message = "User name should be at least 4 characters long")
        @Max(value = 20, message = "User name should be at most 20 characters long")
        String username
) {}
