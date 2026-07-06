package com.ekwe_hub.zeeshopserver.userauth.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserResponse(
        UUID id,
        String username,
        String email,
        String roleName,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
