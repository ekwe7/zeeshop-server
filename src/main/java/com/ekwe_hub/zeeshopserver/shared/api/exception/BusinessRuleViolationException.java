package com.ekwe_hub.zeeshopserver.shared.api.exception;

/**
 * Thrown when an operation violates a domain business rule that cannot be
 * expressed as a simple field validation constraint.
 * Mapped to HTTP 422 Unprocessable Entity by GlobalExceptionHandler.
 *
 * Examples of domain rules that belong here (not in @Valid annotations):
 *   - "Cannot cancel an order that has already been shipped"
 *   - "Cannot add more items than available stock"
 *   - "A coupon cannot be applied to an order below the minimum spend"
 *
 * The message should be user-friendly since it is sent directly to the client.
 *
 * Usage:
 *   throw new BusinessRuleViolationException("Cannot cancel a shipped order");
 */
public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
