package com.ekwe_hub.zeeshopserver.shared.infrastructure.persistence;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Tells Spring JPA Auditing who is performing the current write operation.
 *
 * Right now it returns "system" as a placeholder — meaning all created_by / updated_by
 * columns will contain "system" until authentication is added.
 *
 * When Spring Security is introduced, replace the body of getCurrentAuditor() with:
 *
 *   return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
 *       .filter(Authentication::isAuthenticated)
 *       .map(Authentication::getName);
 *
 * The bean name "systemAuditorAware" must match the auditorAwareRef in
 * @EnableJpaAuditing on ZeeshopServerApplication.
 */
@Component("systemAuditorAware")
public class SystemAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of("system");
    }
}
