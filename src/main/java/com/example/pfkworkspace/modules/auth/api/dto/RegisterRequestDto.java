package com.example.pfkworkspace.modules.auth.api.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank
        @Email(message = "Email should be valid")
        String email,

        @NotBlank
        @Size(min = 5, max = 20, message = "Password should be between 5 and 20 characters long")
        String password,

        @NotBlank
        @Size(min = 4, max = 20, message = "First name should be between 4 and 20 characters long")
        String firstName,

        @NotBlank
        @Size(min = 4, max = 20, message = "Last name should be between 4 and 20 characters long")
        String lastName,

        @NotBlank
        @Size(min = 4, max = 20, message = "User name should be between 4 and 20 characters long")
        String username
) {}
