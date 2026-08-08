package com.ekwe_hub.zeeshopserver.userauth.dto.response;

import com.ekwe_hub.zeeshopserver.userauth.entity.Permission;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Builder
public record RoleResponse(
        UUID id,
        String name,
        Set<Permission> permissions
) {
}
