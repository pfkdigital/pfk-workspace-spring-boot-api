package com.example.pfkworkspace.modules.auth.api;

public class EmailSendingException extends RuntimeException {
  public EmailSendingException(String message, Throwable cause) {
    super(message, cause);
  }
}
