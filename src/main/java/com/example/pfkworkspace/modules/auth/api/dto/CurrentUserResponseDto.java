package com.example.pfkworkspace.modules.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CurrentUserResponseDto {
    private String email;
    private String firstName;
    private String lastName;
    private String username;
}
