package com.ekwe_hub.zeeshopserver.productInventory.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record InventoryAdjustmentResponse(
        UUID id,
        UUID productId,
        String productName,
        String sku,
        int quantityBefore,
        int quantityAfter,
        int change,
        String reason,
        String adjustedBy,
        LocalDateTime adjustedAt
) {
}
