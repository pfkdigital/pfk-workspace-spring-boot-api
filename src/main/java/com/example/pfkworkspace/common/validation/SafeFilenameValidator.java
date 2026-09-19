package com.example.pfkworkspace.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Locale;
import java.util.Set;

public class SafeFilenameValidator implements ConstraintValidator<SafeFilename, String> {

  /** Extensions that are never accepted, regardless of the declared content type. */
  public static final Set<String> BLOCKED_EXTENSIONS =
      Set.of("exe", "msi", "bat", "cmd", "com", "scr", "pif", "dll", "sh", "ps1");

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    if (value.contains("/") || value.contains("\\") || value.contains("\0")) {
      return false;
    }
    int dot = value.lastIndexOf('.');
    if (dot < 0 || dot == value.length() - 1) {
      return true;
    }
    String extension = value.substring(dot + 1).toLowerCase(Locale.ROOT);
    return !BLOCKED_EXTENSIONS.contains(extension);
  }
}
