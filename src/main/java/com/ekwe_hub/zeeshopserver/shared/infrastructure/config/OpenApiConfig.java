package com.ekwe_hub.zeeshopserver.shared.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Registers the OpenAPI / Swagger metadata for the ZeeShop REST API.
 *
 * The generated UI is available at:
 *   http://localhost:{port}/swagger-ui.html   (interactive browser UI)
 *   http://localhost:{port}/api-docs          (raw OpenAPI JSON spec)
 *
 * Server entries determine which base URL Swagger uses when you click "Try it out".
 * The local server is auto-populated from the configured port so developers don't
 * have to change this file when they run on a different port.
 *
 * The "bearerAuth" security scheme adds an "Authorize" button to Swagger UI —
 * paste the access token from POST /api/auth/login there and every "Try it
 * out" call after that carries it automatically.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Value("${server.port:8083}")
    private String serverPort;

    @Bean
    public OpenAPI zeeshopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ZeeShop API")
                        .description("E-commerce platform REST API")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("ZeeShop Team")
                                .email("support@zeeshop.com"))
                        .license(new License()
                                .name("MIT License")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local Development"),
                        new Server().url("https://api.zeeshop.com").description("Production")
                ))
                .components(new Components().addSecuritySchemes(BEARER_AUTH_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_AUTH_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME));
    }
}
