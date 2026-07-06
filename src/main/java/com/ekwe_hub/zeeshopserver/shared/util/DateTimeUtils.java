package com.ekwe_hub.zeeshopserver.shared.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Utility methods for date and time operations across the ZeeShop application.
 *
 * All times are stored and processed in UTC (ZoneOffset.UTC). Conversion to the
 * user's local timezone is the responsibility of the frontend, not the backend.
 * This avoids DST edge cases and keeps the database consistent across deployments
 * in different regions.
 *
 * Non-instantiable — every method is static.
 */
public final class DateTimeUtils {

    // Single shared formatter — DateTimeFormatter is thread-safe so one instance is fine
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private DateTimeUtils() {}

    /** Returns the current date-time in UTC — use this instead of LocalDateTime.now() everywhere. */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    /** Formats a date-time as an ISO-8601 string without milliseconds, e.g. "2026-07-02T10:30:00". */
    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(ISO_FORMATTER) : null;
    }

    /**
     * Returns true when the given expiry time is in the past.
     * Used for checking token/coupon/session expiry.
     */
    public static boolean isExpired(LocalDateTime expiryTime) {
        return expiryTime != null && nowUtc().isAfter(expiryTime);
    }

    public static LocalDateTime plusDays(LocalDateTime base, long days) {
        return base.plusDays(days);
    }

    public static LocalDateTime plusHours(LocalDateTime base, long hours) {
        return base.plusHours(hours);
    }
}
