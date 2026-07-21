package com.ekwe_hub.zeeshopserver.supplierpurchase.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record SupplierResponse(
        UUID id,
        String name,
        String contactName,
        String phone,
        String email,
        String address,
        BigDecimal balance,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
