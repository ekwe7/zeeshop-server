package com.ekwe_hub.zeeshopserver.sales.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleItemRequest(
        @NotNull(message = "Product ID is required")
        UUID productId,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,

        @NotNull(message = "Unit price is required")
        @Min(value = 0, message = "Unit price must not be negative")
        BigDecimal unitPrice
) {}
