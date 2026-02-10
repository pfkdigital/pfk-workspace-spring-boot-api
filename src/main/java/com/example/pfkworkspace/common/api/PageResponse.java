package com.example.pfkworkspace.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PageResponse {
    private boolean success;
    private String message;
    private Object data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
