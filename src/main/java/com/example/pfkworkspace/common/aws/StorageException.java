package com.example.pfkworkspace.common.aws;

/** Raised when an S3 operation (presign, delete, ...) fails at the SDK or service level. */
public class StorageException extends RuntimeException {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
