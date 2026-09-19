package com.example.pfkworkspace.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Locale;
import java.util.Set;

public class AllowedContentTypeValidator
    implements ConstraintValidator<AllowedContentType, String> {

  public static final Set<String> ALLOWED_MIME_TYPES =
      Set.of(
          "application/json",
          "application/xml",
          "application/x-www-form-urlencoded",
          "application/javascript",
          "application/pdf",
          "application/zip",
          "application/vnd.ms-excel",
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
          "application/msword",
          "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
          "application/vnd.ms-powerpoint",
          "application/vnd.openxmlformats-officedocument.presentationml.presentation",
          "application/octet-stream",
          "application/graphql",
          "text/html",
          "text/plain",
          "text/css",
          "text/javascript",
          "text/csv",
          "image/png",
          "image/jpeg",
          "image/gif",
          "image/svg+xml",
          "image/webp",
          "audio/mpeg",
          "audio/ogg",
          "audio/wav",
          "audio/webm",
          "video/mp4",
          "video/webm",
          "video/ogg",
          "font/woff",
          "font/woff2",
          "font/ttf",
          "font/otf",
          "multipart/form-data");

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    // Strip parameters such as "; charset=utf-8" before matching.
    String mediaType = value.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
    return ALLOWED_MIME_TYPES.contains(mediaType);
  }
}
