package com.example.pfkworkspace.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Locale;

public class SafeExtensionValidator implements ConstraintValidator<SafeExtension, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    String extension = value.startsWith(".") ? value.substring(1) : value;
    return !SafeFilenameValidator.BLOCKED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));
  }
}
