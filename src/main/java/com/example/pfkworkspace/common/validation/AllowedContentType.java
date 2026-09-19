package com.example.pfkworkspace.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Validates that a MIME type is in the upload allowlist. Null values are considered valid. */
@Documented
@Constraint(validatedBy = AllowedContentTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedContentType {
  String message() default "Content type is not allowed.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
