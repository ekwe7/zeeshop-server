package com.ekwe_hub.zeeshopserver.shared.api.exception;

/**
 * Thrown when a requested resource does not exist in the database.
 * Mapped to HTTP 404 Not Found by GlobalExceptionHandler.
 *
 * Usage:
 *   throw new ResourceNotFoundException("Product", productId);
 *   // → "Product not found with id: <uuid>"
 *
 *   throw new ResourceNotFoundException("No active cart found for user " + userId);
 *   // → custom message when the ID pattern doesn't fit
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found with id: " + id);
    }
}
