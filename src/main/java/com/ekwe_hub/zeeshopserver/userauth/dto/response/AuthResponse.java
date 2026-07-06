package com.ekwe_hub.zeeshopserver.userauth.dto;

import lombok.Builder;

import java.util.Set;

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        String username,
        String email,
        String role,
        Set<String> permissions
) {
}
