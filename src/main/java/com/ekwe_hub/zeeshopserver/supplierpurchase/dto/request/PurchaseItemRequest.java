package com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseItemRequest(

        @NotNull(message = "Product is required")
        UUID productId,

        @NotNull(message = "Quantity ordered is required")
        @Min(value = 1, message = "Quantity ordered must be at least 1")
        Integer quantityOrdered,

        @NotNull(message = "Unit cost is required")
        @DecimalMin(value = "0.0", message = "Unit cost must not be negative")
        BigDecimal unitCost
) {
}
