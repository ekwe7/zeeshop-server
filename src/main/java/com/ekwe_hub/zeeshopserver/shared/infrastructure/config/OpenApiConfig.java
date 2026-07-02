package com.ekwe_hub.zeeshopserver.shared.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
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
 * To add JWT authentication to the Swagger UI later, add a SecurityScheme bean
 * here and annotate controllers / operations with @SecurityRequirement.
 */
@Configuration
public class OpenApiConfig {

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
                ));
    }
}
