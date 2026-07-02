package com.ekwe_hub.zeeshopserver.shared.infrastructure.persistence;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Tells Spring JPA Auditing who is performing the current write operation.
 *
 * Reads the authenticated principal's username from the security context.
 * Falls back to "system" for writes that happen outside an authenticated
 * request — e.g. AdminUserSeeder running at startup, before any request
 * (and therefore any SecurityContext) exists.
 *
 * The bean name "systemAuditorAware" must match the auditorAwareRef in
 * @EnableJpaAuditing on ZeeshopServerApplication.
 */
@Component("systemAuditorAware")
public class SystemAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of("system");
        }

        return Optional.of(authentication.getName());
    }
}
