package com.example.pfkworkspace.common.error;

import com.example.pfkworkspace.common.api.ApiResponse;
import com.example.pfkworkspace.common.error.BadRequestException;
import com.example.pfkworkspace.common.error.ConflictException;
import com.example.pfkworkspace.common.error.NotFoundException;
import com.example.pfkworkspace.common.error.UnauthorizedException;
import com.example.pfkworkspace.modules.auth.api.EmailSendingException;
import com.example.pfkworkspace.modules.workspace.api.exception.WorkspaceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex) {
    List<String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .collect(Collectors.toList());
    String message =
        errors.isEmpty()
            ? "Validation failed for the request body."
            : "Validation failed. Please correct the highlighted fields.";
    return buildResponse(HttpStatus.BAD_REQUEST, message, errors);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse> handleConstraintViolation(ConstraintViolationException ex) {
    List<String> errors =
        ex.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .collect(Collectors.toList());
    String message =
        errors.isEmpty()
            ? "Validation failed for request parameters."
            : "Validation failed. Please correct the request parameters.";
    return buildResponse(HttpStatus.BAD_REQUEST, message, errors);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse> handleMissingParam(
      MissingServletRequestParameterException ex) {
    String message = "Missing required parameter: " + ex.getParameterName();
    return buildResponse(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse> handleUnreadable(HttpMessageNotReadableException ex) {
    String message = "Request body is malformed or unreadable.";
    return buildResponse(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException ex) {
    String message = safeMessage(ex.getMessage(), "Invalid request.");
    return buildResponse(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiResponse> handleBadRequest(BadRequestException ex) {
    String message = safeMessage(ex.getMessage(), "Invalid request.");
    return buildResponse(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiResponse> handleConflict(ConflictException ex) {
    String message = safeMessage(ex.getMessage(), "Conflict.");
    return buildResponse(HttpStatus.CONFLICT, message);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiResponse> handleNotFound(NotFoundException ex) {
    String message = safeMessage(ex.getMessage(), "Resource not found.");
    return buildResponse(HttpStatus.NOT_FOUND, message);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiResponse> handleUnauthorized(UnauthorizedException ex) {
    String message = safeMessage(ex.getMessage(), "Authentication is required.");
    return buildResponse(HttpStatus.UNAUTHORIZED, message);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse> handleBadCredentials(BadCredentialsException ex) {
    String message = "Invalid username or password.";
    return buildResponse(HttpStatus.UNAUTHORIZED, message);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse> handleAuthentication(AuthenticationException ex) {
    String message = safeMessage(ex.getMessage(), "Authentication failed.");
    return buildResponse(HttpStatus.UNAUTHORIZED, message);
  }

  @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
  public ResponseEntity<ApiResponse> handleAuthRequired(
      AuthenticationCredentialsNotFoundException ex) {
    String message = safeMessage(ex.getMessage(), "Authentication is required.");
    return buildResponse(HttpStatus.UNAUTHORIZED, message);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException ex) {
    String message =
        safeMessage(ex.getMessage(), "You do not have permission to perform this action.");
    return buildResponse(HttpStatus.FORBIDDEN, message);
  }

  @ExceptionHandler(EmailSendingException.class)
  public ResponseEntity<ApiResponse> handleEmailSending(EmailSendingException ex) {
    String message = "Email service is temporarily unavailable. Please try again later.";
    return buildResponse(HttpStatus.BAD_GATEWAY, message);
  }

  @ExceptionHandler(WorkspaceNotFoundException.class)
  public ResponseEntity<ApiResponse> handleWorkspaceNotFound(WorkspaceNotFoundException exception) {
    return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse> handleUnexpected(Exception ex) {
    String message = safeMessage(ex.getMessage(), "Unexpected error occurred.");
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, message);
  }

  private ResponseEntity<ApiResponse> buildResponse(HttpStatus status, String message) {
    return buildResponse(status, message, null);
  }

  private ResponseEntity<ApiResponse> buildResponse(
      HttpStatus status, String message, List<String> errors) {
    ApiError apiError =
        ApiError.builder()
            .status(status)
            .message(message)
            .timestamp(Instant.now())
            .errors(errors)
            .build();
    ApiResponse response =
        ApiResponse.builder().success(false).message(message).data(apiError).build();
    return new ResponseEntity<>(response, status);
  }

  private String formatFieldError(FieldError error) {
    String field = error.getField();
    String defaultMessage = error.getDefaultMessage();
    if (defaultMessage == null || defaultMessage.isBlank()) {
      return field + ": invalid value";
    }
    return field + ": " + defaultMessage;
  }

  private String safeMessage(String message, String fallback) {
    if (message == null || message.isBlank()) {
      return fallback;
    }
    return message;
  }
}
