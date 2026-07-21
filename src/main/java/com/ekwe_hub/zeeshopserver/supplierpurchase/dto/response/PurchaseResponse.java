package com.ekwe_hub.zeeshopserver.supplierpurchase.dto.response;

import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.PurchaseStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Also serves as the purchase invoice view — supplier details, line items
 * and totals are everything a printed invoice needs.
 */
@Builder
public record PurchaseResponse(
        UUID id,
        UUID supplierId,
        String supplierName,
        PurchaseStatus status,
        String invoiceNumber,
        String notes,
        BigDecimal totalAmount,
        List<PurchaseItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
