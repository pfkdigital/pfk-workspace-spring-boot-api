package com.example.pfkworkspace.modules.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequestDto (
    @NotBlank
    @Size(min = 5, max = 20, message = "Password should be between 5 and 20 characters long")
    String newPassword
) {}
