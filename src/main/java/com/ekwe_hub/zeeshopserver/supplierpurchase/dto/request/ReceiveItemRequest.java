package com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReceiveItemRequest(

        @NotNull(message = "Purchase item is required")
        UUID purchaseItemId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {
}
