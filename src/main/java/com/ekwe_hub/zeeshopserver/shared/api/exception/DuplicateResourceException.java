package com.ekwe_hub.zeeshopserver.shared.api.exception;

/**
 * Thrown when creating a resource would violate a uniqueness constraint.
 * Mapped to HTTP 409 Conflict by GlobalExceptionHandler.
 *
 * Use this instead of letting the database constraint bubble up as a
 * DataIntegrityViolationException, which would be caught by the generic 500 handler
 * and expose nothing useful to the client.
 *
 * Usage:
 *   throw new DuplicateResourceException("User", "email", email);
 *   // → "User already exists with email: john@example.com"
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resource, String field, Object value) {
        super(resource + " already exists with " + field + ": " + value);
    }
}
