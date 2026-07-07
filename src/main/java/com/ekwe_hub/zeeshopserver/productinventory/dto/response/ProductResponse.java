package com.ekwe_hub.zeeshopserver.productinventory.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ProductResponse(
        UUID id,
        String name,
        String sku,
        String description,
        BigDecimal price,
        String categoryName,
        String unitName,
        int quantityOnHand,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
