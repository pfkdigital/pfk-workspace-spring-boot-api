package com.example.pfkworkspace.modules.task.api.dto.request;

import com.example.pfkworkspace.common.validation.AllowedContentType;
import com.example.pfkworkspace.common.validation.SafeExtension;
import com.example.pfkworkspace.common.validation.SafeFilename;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateAttachmentRequestDto(
        @NotBlank
        @Size(max = 255, message = "Filename must be at most 255 characters.")
        @SafeFilename(message = "Executable files are not allowed.")
        String filename,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9]{1,16}$", message = "Extension must be 1-16 alphanumeric characters without a leading dot.")
        @SafeExtension(message = "Executable files are not allowed.")
        String extension,

        @NotBlank
        @AllowedContentType(message = "Unsupported file type.")
        String contentType,

        @NotNull
        @Positive(message = "File size must be greater than 0.")
        @Max(value = MAX_SIZE_BYTES, message = "File size must not exceed 25 MB.")
        Long sizeBytes,

        @NotBlank
        @Pattern(regexp = "^[a-fA-F0-9]{64}$", message = "Checksum must be a hex-encoded SHA-256 digest.")
        String checksum
) {
    public static final long MAX_SIZE_BYTES = 25L * 1024 * 1024;
}
