package com.ekwe_hub.zeeshopserver.shared.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordEncodingTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void matchesTheCorrectRawPasswordAgainstItsHash() {
        String hash = encoder.encode("S3cur3P@ss");

        assertTrue(encoder.matches("S3cur3P@ss", hash));
    }

    @Test
    void rejectsAnIncorrectPassword() {
        String hash = encoder.encode("S3cur3P@ss");

        assertFalse(encoder.matches("wrong-password", hash));
    }

    @Test
    void neverStoresThePlaintextPassword() {
        String raw = "S3cur3P@ss";
        String hash = encoder.encode(raw);

        assertNotEquals(raw, hash);
        assertTrue(hash.startsWith("$2"));
    }

    @Test
    void saltsSoTheSamePasswordHashesDifferentlyEachTime() {
        String raw = "S3cur3P@ss";

        assertNotEquals(encoder.encode(raw), encoder.encode(raw));
    }
}
