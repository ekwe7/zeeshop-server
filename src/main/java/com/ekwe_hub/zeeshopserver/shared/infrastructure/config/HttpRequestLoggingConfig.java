package com.ekwe_hub.zeeshopserver.shared.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Registers a servlet filter that logs every inbound HTTP request.
 *
 * Why this filter and not AOP or a HandlerInterceptor?
 * CommonsRequestLoggingFilter sits at the servlet level, so it captures requests
 * that never reach a controller (e.g. 404s from wrong paths) and it buffers the
 * request body safely so the controller can still read it.
 *
 * Log output is controlled by the log level of CommonsRequestLoggingFilter in
 * logback-spring.xml (set to DEBUG). Setting it to INFO or above silences request
 * logging without removing this config — useful for production where body logging
 * could expose PII.
 *
 * Payload is capped at 1000 chars to avoid flooding logs when large JSON bodies
 * (e.g. bulk product uploads) come in.
 */
@Configuration
public class HttpRequestLoggingConfig {

    @Bean
    public CommonsRequestLoggingFilter httpRequestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(1000);
        // Headers excluded — they contain auth tokens which must not be logged
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
    }
}
