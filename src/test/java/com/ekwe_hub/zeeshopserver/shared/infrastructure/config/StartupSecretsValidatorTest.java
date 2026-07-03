package com.ekwe_hub.zeeshopserver.shared.infrastructure.config;

import org.junit.jupiter.api.Test;

import static com.ekwe_hub.zeeshopserver.shared.infrastructure.config.StartupSecretsValidator.DEFAULT_ADMIN_PASSWORD;
import static com.ekwe_hub.zeeshopserver.shared.infrastructure.config.StartupSecretsValidator.DEFAULT_JWT_SECRET;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StartupSecretsValidatorTest {

    @Test
    void allowsDefaultSecretsWhenEnvironmentIsLocal() {
        var validator = new StartupSecretsValidator("local", DEFAULT_JWT_SECRET, DEFAULT_ADMIN_PASSWORD);

        assertDoesNotThrow(validator::validate);
    }

    @Test
    void rejectsTheDefaultJwtSecretOutsideLocal() {
        var validator = new StartupSecretsValidator("production", DEFAULT_JWT_SECRET, "a-real-admin-password");

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validate);
        assertTrue(exception.getMessage().contains("JWT_SECRET"));
    }

    @Test
    void rejectsTheDefaultAdminPasswordOutsideLocal() {
        var validator = new StartupSecretsValidator("production", "a-real-secret-value-1234567890", DEFAULT_ADMIN_PASSWORD);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validate);
        assertTrue(exception.getMessage().contains("ADMIN_PASSWORD"));
    }

    @Test
    void allowsRealSecretsOutsideLocal() {
        var validator = new StartupSecretsValidator(
                "production", "a-real-secret-value-1234567890", "a-real-admin-password");

        assertDoesNotThrow(validator::validate);
    }
}
