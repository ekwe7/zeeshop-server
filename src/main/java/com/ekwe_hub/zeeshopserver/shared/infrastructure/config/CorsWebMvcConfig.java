package com.ekwe_hub.zeeshopserver.shared.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures Cross-Origin Resource Sharing (CORS) for the ZeeShop API.
 *
 * Without this, browsers block requests from the frontend (e.g. React on port 3000)
 * to this backend (port 8083) because they are different origins.
 *
 * Rules:
 *   - Only paths under /api/** are exposed to cross-origin callers.
 *   - Allowed origins are driven by the CORS_ALLOWED_ORIGINS environment variable,
 *     so local dev, staging, and production each have their own list without
 *     changing code.
 *   - Credentials (cookies / auth headers) are allowed because the frontend will
 *     send JWT tokens in headers.
 *   - maxAge caches the preflight response in the browser, reducing OPTIONS round-trips.
 */
@Configuration
public class CorsWebMvcConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String[] allowedMethods;

    @Value("${cors.allowed-headers}")
    private String[] allowedHeaders;

    @Value("${cors.max-age}")
    private long maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders)
                .allowCredentials(true)
                .maxAge(maxAge);
    }
}
