package com.ekwe_hub.zeeshopserver.shared.infrastructure.security;

import com.ekwe_hub.zeeshopserver.userauth.entity.Role;
import com.ekwe_hub.zeeshopserver.userauth.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-value-1234567890abcdef";

    private final JwtService jwtService = new JwtService(SECRET, 60_000);

    @Test
    void generatesATokenThatIsValidAndCarriesTheUsername() {
        String token = jwtService.generateAccessToken(principal("alice"));

        assertTrue(jwtService.isValid(token));
        assertEquals("alice", jwtService.extractUsername(token));
    }

    @Test
    void rejectsATokenSignedWithADifferentSecret() {
        JwtService otherService = new JwtService("a-completely-different-secret-value-000", 60_000);
        String token = otherService.generateAccessToken(principal("bob"));

        assertFalse(jwtService.isValid(token));
    }

    @Test
    void rejectsAnExpiredToken() throws InterruptedException {
        JwtService shortLived = new JwtService(SECRET, 1);
        String token = shortLived.generateAccessToken(principal("carol"));

        Thread.sleep(20);

        assertFalse(shortLived.isValid(token));
    }

    // @Test
    // void rejectsMalformedInput() {
    //     assertFalse(jwtService.isValid("not-a-real-token"));
    // }

    private UserPrincipal principal(String username) {
        User user = User.builder()
                .username(username)
                .email(username + "@example.com")
                .password("irrelevant-hash")
                .role(Role.builder().name("STAFF").permissions(Set.of()).build())
                .enabled(true)
                .build();
        user.setId(UUID.randomUUID());
        return new UserPrincipal(user);
    }

}
