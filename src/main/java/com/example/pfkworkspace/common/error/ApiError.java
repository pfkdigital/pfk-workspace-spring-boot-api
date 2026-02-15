package com.example.pfkworkspace.common.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ApiError {
    private HttpStatus status;
    private String message;
    private Instant timestamp;
    private List<String> errors;
}
