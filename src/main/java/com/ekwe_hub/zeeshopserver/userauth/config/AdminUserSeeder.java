package com.ekwe_hub.zeeshopserver.userauth.config;

import com.ekwe_hub.zeeshopserver.userauth.entity.Permission;
import com.ekwe_hub.zeeshopserver.userauth.entity.Role;
import com.ekwe_hub.zeeshopserver.userauth.entity.User;
import com.ekwe_hub.zeeshopserver.userauth.repository.RoleRepository;
import com.ekwe_hub.zeeshopserver.userauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Ensures an ADMIN role and one admin user exist on startup, since there is
 * no public registration endpoint (users are provisioned by an admin, not
 * self-service). Only acts when the users table is empty, so it never
 * overwrites an already-configured system, and password/email come from
 * environment variables so no real credential is ever hardcoded.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.seed.username}")
    private String adminUsername;

    @Value("${admin.seed.email}")
    private String adminEmail;

    @Value("${admin.seed.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ADMIN")
                        .permissions(Set.of(Permission.values()))
                        .build()));

        User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(adminRole)
                .enabled(true)
                .build();
        userRepository.save(admin);

        log.info("Seeded default admin user '{}' — change its password immediately in non-local environments", adminUsername);
    }
}
