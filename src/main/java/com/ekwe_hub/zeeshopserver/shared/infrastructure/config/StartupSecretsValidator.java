package com.ekwe_hub.zeeshopserver.shared.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Refuses to start the app if it's running outside "local" (app.environment) while
 * still carrying the checked-in default JWT secret or admin password.
 *
 * These defaults exist so the app boots with zero setup on a developer machine, but
 * that same convenience becomes a vulnerability if silently carried into any shared
 * environment — there was previously nothing stopping that from happening.
 */
@Component
public class StartupSecretsValidator {

    static final String DEFAULT_JWT_SECRET = "dev-only-secret-do-not-use-in-production-1234567890abcdef";
    static final String DEFAULT_ADMIN_PASSWORD = "ChangeMe123!";

    private final String environment;
    private final String jwtSecret;
    private final String adminPassword;

    public StartupSecretsValidator(
            @Value("${app.environment}") String environment,
            @Value("${security.jwt.secret}") String jwtSecret,
            @Value("${admin.seed.password}") String adminPassword) {
        this.environment = environment;
        this.jwtSecret = jwtSecret;
        this.adminPassword = adminPassword;
    }

    @PostConstruct
    void validate() {
        if ("local".equalsIgnoreCase(environment)) {
            return;
        }

        if (DEFAULT_JWT_SECRET.equals(jwtSecret)) {
            throw new IllegalStateException(
                    "Refusing to start: app.environment is '" + environment + "' but JWT_SECRET is still "
                            + "the default placeholder. Set a real JWT_SECRET for this environment.");
        }

        if (DEFAULT_ADMIN_PASSWORD.equals(adminPassword)) {
            throw new IllegalStateException(
                    "Refusing to start: app.environment is '" + environment + "' but ADMIN_PASSWORD is still "
                            + "the default placeholder. Set a real ADMIN_PASSWORD for this environment.");
        }
    }
}
