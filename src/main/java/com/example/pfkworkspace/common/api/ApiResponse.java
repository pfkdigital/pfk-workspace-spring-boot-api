package com.example.pfkworkspace.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;
}
