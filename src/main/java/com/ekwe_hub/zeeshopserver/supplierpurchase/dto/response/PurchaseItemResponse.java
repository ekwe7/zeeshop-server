package com.ekwe_hub.zeeshopserver.supplierpurchase.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PurchaseItemResponse(
        UUID id,
        UUID productId,
        String productName,
        int quantityOrdered,
        int quantityReceived,
        BigDecimal unitCost,
        BigDecimal lineTotal
) {
}
