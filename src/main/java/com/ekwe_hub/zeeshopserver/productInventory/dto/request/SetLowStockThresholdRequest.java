package com.ekwe_hub.zeeshopserver.productInventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SetLowStockThresholdRequest(

        @NotNull(message = "Threshold is required")
        @Min(value = 0, message = "Threshold must not be negative")
        Integer threshold
) {
}
