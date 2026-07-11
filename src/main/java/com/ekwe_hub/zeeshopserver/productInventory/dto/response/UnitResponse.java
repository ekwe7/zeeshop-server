package com.ekwe_hub.zeeshopserver.productInventory.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UnitResponse(
        UUID id,
        String name,
        String symbol,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
