package com.ekwe_hub.zeeshopserver.shared.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Uniform envelope for every HTTP response the ZeeShop API sends.
 *
 * All endpoints return this structure so clients never have to guess the shape:
 *
 *   {
 *     "success": true,
 *     "message": "Operation successful",
 *     "data": { ... },          // present on success
 *     "errors": { ... },        // present on validation failure (field → error message)
 *     "timestamp": "2026-07-02T10:30:00"
 *   }
 *
 * @JsonInclude(NON_NULL) keeps null fields out of the JSON response.
 * A success response omits "errors"; an error response omits "data".
 * This avoids sending {"data": null} which confuses some clients.
 *
 * Static factory methods (success, created, error) are the only way to build
 * a response — the @Builder is private to ensure consistent state.
 *
 * @param <T> the type of the payload inside "data"
 */

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;

    // Field-level validation errors: key = field name, value = error message
    private final Map<String, String> errors;

    private final LocalDateTime timestamp;

    // --- Success factories ---

    /** Use when returning an existing resource (GET, PUT, PATCH). */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation successful");
    }

    /** Use for POST endpoints that create a new resource — maps to HTTP 201. */
    public static <T> ApiResponse<T> created(T data) {
        return success(data, "Resource created successfully");
    }

    // --- Error factories ---

    /** Use for single-message errors (404, 409, 500, etc.). */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /** Use for validation errors where each field has its own error message. */
    public static <T> ApiResponse<T> error(String message, Map<String, String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
