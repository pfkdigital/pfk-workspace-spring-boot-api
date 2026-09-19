package com.example.pfkworkspace.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a bare file extension (no leading dot) is not in the blocked (executable) list.
 * Null values are considered valid.
 */
@Documented
@Constraint(validatedBy = SafeExtensionValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeExtension {
  String message() default "File extension is not allowed.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
