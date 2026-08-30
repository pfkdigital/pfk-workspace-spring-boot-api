package com.example.pfkworkspace.modules.task.api.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AddCommentResponseDto {
    private UUID id;
    private UUID taskId;
    private String body;
    private AuthorDto author;
    private Instant editedAt;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthorDto {
        private UUID id;
        private String username;
        private String firstName;
        private String lastName;
    }
}
