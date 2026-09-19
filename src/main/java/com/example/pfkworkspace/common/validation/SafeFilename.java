package com.example.pfkworkspace.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a filename has no path segments and does not use a blocked (executable)
 * extension. Null values are considered valid.
 */
@Documented
@Constraint(validatedBy = SafeFilenameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeFilename {
  String message() default "Filename is not allowed.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
