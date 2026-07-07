package com.ekwe_hub.zeeshopserver.productinventory.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record InventoryResponse(
        UUID id,
        UUID productId,
        String productName,
        String sku,
        int quantityOnHand,
        LocalDateTime updatedAt
) {
}
